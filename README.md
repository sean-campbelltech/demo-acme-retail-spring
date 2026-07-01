# ACME Retail — Order Management System

A deliberately "legacy-flavoured" Spring Boot service that prices customer orders.
It is the starting point for the Diffblue Cover lectures: a complete, compiling
application that ships with **no tests at all**. Over the following lessons we use
the Diffblue Cover IntelliJ plugin and CLI to generate a regression test suite for
it.

> There is intentionally nothing in `src/test`. Generating those tests is the
> whole exercise.

## What it does

The service brings together three subsystems that, in a real business, would have
accreted rules over many years:

- **Discounting** — automatic loyalty-tier discounts, coupon validation, percentage
  / fixed-amount / free-shipping / buy-x-get-y coupons, and a stacked-discount cap.
- **Shipping** — method base rates, per-kilogram surcharges, destination zone
  multipliers, fuel surcharges, handling and hazardous-goods fees, weight limits and
  free-shipping thresholds.
- **Tax** — destination- and tax-class-aware sales tax, applied line by line with the
  order discount allocated proportionally.

`PricingService` orchestrates all three to turn an `Order` into a fully itemised
`PriceBreakdown`.

## Tech stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.x (Spring Framework 7) |
| Build | Gradle (wrapper included — no local Gradle install needed) |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Web | Spring MVC REST controllers |

The Gradle build uses a Java **toolchain** pinned to Java 21. If you do not have a
JDK 21 installed, Gradle will download one automatically the first time you build.

## Build & run

```bash
# from this directory
./gradlew bootRun        # start the service on http://localhost:8080
./gradlew build          # compile + assemble the bootable jar
```

On Windows use `gradlew.bat` instead of `./gradlew`.

The application seeds an in-memory database on startup with a small catalogue,
three customers, six coupons and five shipping zones, so it is immediately usable.

- API base path: `http://localhost:8080/api`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:oms`, user `sa`)
- Health: `http://localhost:8080/actuator/health`

### Try it

```bash
# list the catalogue
curl http://localhost:8080/api/products

# create a draft order for Dana (a SILVER-tier customer) and attach a coupon
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
        "customerId": 2,
        "shippingMethod": "STANDARD",
        "lines": [
          {"sku": "ELEC-HEADPHONES-01", "quantity": 1},
          {"sku": "ELEC-CHARGER-USC",   "quantity": 2}
        ],
        "couponCode": "WELCOME10"
      }'

# itemised price quote for order 1
curl http://localhost:8080/api/orders/1/quote

# place the order (verifies stock, reserves it, persists totals)
curl -X POST http://localhost:8080/api/orders/1/place
```

### Seeded data

| Customers (id) | Tier | Notes |
|---|---|---|
| 1 | NONE | domestic (US) |
| 2 | SILVER | domestic (US) |
| 3 | PLATINUM | tax-exempt, ships to GB |

| Coupons | Type |
|---|---|
| `WELCOME10` | 10% off (min spend $25) |
| `SAVE15` | $15 off (min spend $100) |
| `FREESHIP` | free shipping (BRONZE+) |
| `BUY2GET1` | buy 2 get 1 free |
| `PLATINUM20` | 20% off (PLATINUM only) |
| `LASTYEAR` | expired (for negative tests) |

## Architecture

```
com.acmeretail.oms
├── domain
│   ├── model      JPA entities (Order, OrderLine, Customer, Product, Coupon, …)
│   ├── enums      LoyaltyTier, OrderStatus, DiscountType, ShippingMethod, TaxClass
│   └── vo         Money — an immutable money value object
├── repository     Spring Data JPA repositories
├── service        Business logic
│   ├── pricing    PriceBreakdown / ShippingQuote / DiscountResult result types
│   └── support    OrderNumberGenerator
├── web            REST controllers, DTOs, response mapper, error handling
├── config         OmsProperties, DataInitializer
└── exception      Domain exceptions
```

## Good targets for generated tests

These classes are dense with branching business logic and make the most compelling
demonstrations of Diffblue Cover:

- `domain.vo.Money` — pure arithmetic, rounding, currency-mismatch guards.
- `service.DiscountService` — coupon applicability, discount maths, the stacked cap.
- `service.ShippingService` — the layered shipping charge and zone resolution.
- `service.TaxService` — rate lookup and tax-class handling.
- `service.PricingService` — the end-to-end orchestration.
- `domain.enums.*` and the entity helper methods (`OrderStatus.canTransitionTo`,
  `InventoryItem.availableQuantity`, `Coupon.isWithinValidityWindow`, …).

## Generating tests with Diffblue Cover

### IntelliJ plugin

1. Open this project in IntelliJ IDEA (import as a Gradle project).
2. Let Gradle finish syncing and building.
3. Right-click a class, method, or package — e.g. `DiscountService` — and choose
   **Diffblue Cover ▸ Write Tests**.
4. Cover analyses the code, generates JUnit tests under `src/test/java`, and runs
   them to confirm they pass.

### CLI

```bash
# build first so Cover has compiled classes to analyse
./gradlew build

# generate tests for the whole project …
dcover create

# … or for a single class
dcover create com.acmeretail.oms.service.DiscountService
```

The generated tests are written to `src/test/java` and become a regression suite
that pins the current behaviour of this legacy code.
