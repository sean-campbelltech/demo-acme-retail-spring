# ACME Retail — Pricing Engine

A deliberately "legacy-flavoured" Spring Boot module that holds the calculation
core of a retail order-management system: the discount and tax logic, built on an
immutable money value object. It is the starting point for the Diffblue Cover
lectures: a small, complete, compiling application that ships with **no tests at
all**. Over the following lessons we use the Diffblue Cover IntelliJ plugin and CLI
to generate a regression test suite for it.

> There is intentionally nothing in `src/test`. Generating those tests is the
> whole exercise.

> **This is a trimmed-down "mini" edition.** The original demo also carried an
> order/customer/coupon domain, a `PricingService` orchestrator, a shipping
> subsystem and a REST/web layer. Those have been removed, and the remaining
> classes trimmed to their core, so that Diffblue Cover generates only a modest
> number of tests per run — small enough to regenerate the whole suite several
> times within a trial licence's usage limit. What is left is the branch-dense
> arithmetic that makes the most compelling test-generation demo.

## What it does

The module keeps two calculation subsystems that, in a real business, would have
accreted rules over many years:

- **Discounting** — automatic loyalty-tier discounts, the buy-x-get-y promotion
  calculation, and a stacked-discount cap that protects margin.
- **Tax** — destination- and tax-class-aware sales tax on a taxable amount.

All amounts flow through `domain.vo.Money`, an immutable money value object with
its own rounding and currency-mismatch guards.

## Tech stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.x (Spring Framework 7) |
| Build | Gradle (wrapper included — no local Gradle install needed) |

The Gradle build uses a Java **toolchain** pinned to Java 21. If you do not have a
JDK 21 installed, Gradle will download one automatically the first time you build.

## Build & run

```bash
# from this directory
./gradlew build          # compile + assemble the bootable jar
./gradlew bootRun        # start the application on http://localhost:
```

On Windows use `gradlew.bat` instead of `./gradlew`.

The services (`DiscountService`, `TaxService`) are plain Spring beans and `Money`
is a plain value object; there is no REST layer in this mini edition, so the
interesting behaviour is exercised through the generated unit tests rather than
HTTP calls.

- Health: `http://localhost:8080/actuator/health`

### Check it's running

With the application started (`./gradlew bootRun`), confirm it is up:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

## Architecture

```
com.acmeretail.oms
├── domain
│   ├── enums      LoyaltyTier, TaxClass
│   └── vo         Money — an immutable money value object
└── service        Business logic
                   DiscountService, TaxService
```

## Good targets for generated tests

Every remaining class carries logic worth pinning:

- `domain.vo.Money` — pure arithmetic, rounding, currency-mismatch guards.
- `service.DiscountService` — loyalty discount, buy-x-get-y maths, the stacked cap.
- `service.TaxService` — rate lookup and tax-class handling.
- `domain.enums.*` — the helper methods on `LoyaltyTier` and `TaxClass`.

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
