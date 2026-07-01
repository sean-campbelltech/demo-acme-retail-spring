package com.acmeretail.oms.config;

import com.acmeretail.oms.domain.enums.DiscountType;
import com.acmeretail.oms.domain.enums.LoyaltyTier;
import com.acmeretail.oms.domain.enums.TaxClass;
import com.acmeretail.oms.domain.model.Address;
import com.acmeretail.oms.domain.model.Category;
import com.acmeretail.oms.domain.model.Coupon;
import com.acmeretail.oms.domain.model.Customer;
import com.acmeretail.oms.domain.model.InventoryItem;
import com.acmeretail.oms.domain.model.Product;
import com.acmeretail.oms.domain.model.ShippingZone;
import com.acmeretail.oms.repository.CategoryRepository;
import com.acmeretail.oms.repository.CouponRepository;
import com.acmeretail.oms.repository.CustomerRepository;
import com.acmeretail.oms.repository.InventoryItemRepository;
import com.acmeretail.oms.repository.ProductRepository;
import com.acmeretail.oms.repository.ShippingZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the in-memory database with a representative catalogue, customers, coupons
 * and shipping zones so the application is usable the moment it starts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ShippingZoneRepository shippingZoneRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           InventoryItemRepository inventoryItemRepository,
                           CustomerRepository customerRepository,
                           CouponRepository couponRepository,
                           ShippingZoneRepository shippingZoneRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.shippingZoneRepository = shippingZoneRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        seedShippingZones();
        Catalogue catalogue = seedCatalogue();
        seedInventory(catalogue);
        seedCustomers();
        seedCoupons();
        log.info("Seeded {} products, {} customers, {} coupons across {} shipping zones",
                productRepository.count(),
                customerRepository.count(),
                couponRepository.count(),
                shippingZoneRepository.count());
    }

    private void seedShippingZones() {
        shippingZoneRepository.save(zone("CA", "California & West", "US",
                "1.00", "0.0500", "75.00", false, "30.000"));
        shippingZoneRepository.save(zone("NE", "North-East", "US",
                "1.05", "0.0500", "75.00", false, "30.000"));
        shippingZoneRepository.save(remoteZone("AK", "Alaska & Remote", "US",
                "1.85", "0.0900", null, "20.000"));
        shippingZoneRepository.save(zone("GB", "United Kingdom", "GB",
                "1.60", "0.0700", "120.00", false, "25.000"));
        shippingZoneRepository.save(zone("EU", "Western Europe", "DE",
                "1.75", "0.0700", "150.00", false, "25.000"));
    }

    private ShippingZone zone(String code, String name, String country, String multiplier,
                              String fuel, String freeThreshold, boolean remote, String maxWeight) {
        ShippingZone zone = new ShippingZone(code, name, country, new BigDecimal(multiplier));
        zone.setFuelSurchargeRate(new BigDecimal(fuel));
        if (freeThreshold != null) {
            zone.setFreeShippingThreshold(new BigDecimal(freeThreshold));
        }
        zone.setRemote(remote);
        if (maxWeight != null) {
            zone.setMaxWeightKg(new BigDecimal(maxWeight));
        }
        return zone;
    }

    private ShippingZone remoteZone(String code, String name, String country, String multiplier,
                                    String fuel, String freeThreshold, String maxWeight) {
        return zone(code, name, country, multiplier, fuel, freeThreshold, true, maxWeight);
    }

    private Catalogue seedCatalogue() {
        Category electronics = categoryRepository.save(category("Electronics", TaxClass.STANDARD, false));
        Category books = categoryRepository.save(category("Books", TaxClass.ZERO_RATED, false));
        Category groceries = categoryRepository.save(category("Groceries", TaxClass.REDUCED, false));
        Category software = categoryRepository.save(category("Software", TaxClass.STANDARD, false));
        Category cleaning = categoryRepository.save(category("Cleaning Supplies", TaxClass.STANDARD, true));

        Product headphones = productRepository.save(
                physical("ELEC-HEADPHONES-01", "Wireless Noise-Cancelling Headphones", "199.99", "0.350", electronics));
        Product charger = productRepository.save(
                physical("ELEC-CHARGER-USC", "65W USB-C Fast Charger", "39.95", "0.180", electronics));
        Product novel = productRepository.save(
                physical("BOOK-NOVEL-1138", "The Long Way Home (Paperback)", "14.99", "0.450", books));
        Product oliveOil = productRepository.save(
                physical("GROC-OLIVEOIL-1L", "Extra Virgin Olive Oil 1L", "12.50", "1.050", groceries));
        Product detergent = productRepository.save(
                physical("CLEAN-BLEACH-2L", "Industrial Bleach 2L", "6.75", "2.200", cleaning));
        Product ide = productRepository.save(
                digital("SOFT-IDE-PRO-YR", "DevStudio Pro (1-year licence)", "149.00", software));

        // A non-discountable, premium item to exercise discountable filtering.
        Product cameraBundle = productRepository.save(
                physical("ELEC-CAMERA-KIT", "Mirrorless Camera Bundle", "1299.00", "1.400", electronics));
        cameraBundle.setDiscountable(false);
        productRepository.save(cameraBundle);

        return new Catalogue(headphones, charger, novel, oliveOil, detergent, ide, cameraBundle);
    }

    private void seedInventory(Catalogue catalogue) {
        inventoryItemRepository.save(stock(catalogue.headphones, 120, 20, false));
        inventoryItemRepository.save(stock(catalogue.charger, 500, 50, false));
        inventoryItemRepository.save(stock(catalogue.novel, 40, 10, true));
        inventoryItemRepository.save(stock(catalogue.oliveOil, 8, 12, false));
        inventoryItemRepository.save(stock(catalogue.detergent, 0, 5, false));
        inventoryItemRepository.save(stock(catalogue.cameraBundle, 3, 1, false));
        // The digital licence is not stock-controlled, so it has no inventory row.
    }

    private void seedCustomers() {
        Customer walkIn = new Customer("Casual", "Shopper", "casual.shopper@example.com");
        walkIn.setLoyaltyTier(LoyaltyTier.NONE);
        walkIn.setLifetimeSpend(new BigDecimal("120.00"));
        walkIn.setDefaultShippingAddress(new Address(
                "742 Evergreen Terrace", null, "Springfield", "CA", "90210", "US"));
        customerRepository.save(walkIn);

        Customer regular = new Customer("Dana", "Reyes", "dana.reyes@example.com");
        regular.setLoyaltyTier(LoyaltyTier.SILVER);
        regular.setLifetimeSpend(new BigDecimal("4200.00"));
        regular.setDefaultShippingAddress(new Address(
                "19 Beacon Street", "Apt 3", "Boston", "NE", "02108", "US"));
        customerRepository.save(regular);

        Customer vip = new Customer("Morgan", "Patel", "morgan.patel@example.com");
        vip.setLoyaltyTier(LoyaltyTier.PLATINUM);
        vip.setLifetimeSpend(new BigDecimal("31850.00"));
        vip.setTaxExempt(true);
        vip.setDefaultShippingAddress(new Address(
                "1 Royal Mile", null, "London", null, "EH1 1AA", "GB"));
        customerRepository.save(vip);
    }

    private void seedCoupons() {
        Coupon welcome = new Coupon("WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10"));
        welcome.setMinimumSpend(new BigDecimal("25.00"));
        welcome.setValidFrom(LocalDate.now().minusMonths(1));
        welcome.setValidUntil(LocalDate.now().plusMonths(6));
        welcome.setMaxRedemptions(10_000);
        welcome.setStackable(true);
        couponRepository.save(welcome);

        Coupon fifteenOff = new Coupon("SAVE15", DiscountType.FIXED_AMOUNT, new BigDecimal("15.00"));
        fifteenOff.setMinimumSpend(new BigDecimal("100.00"));
        fifteenOff.setValidUntil(LocalDate.now().plusMonths(2));
        couponRepository.save(fifteenOff);

        Coupon freeShip = new Coupon("FREESHIP", DiscountType.FREE_SHIPPING, BigDecimal.ZERO);
        freeShip.setMinimumTier(LoyaltyTier.BRONZE);
        couponRepository.save(freeShip);

        Coupon bogo = new Coupon("BUY2GET1", DiscountType.BUY_X_GET_Y, BigDecimal.ZERO);
        bogo.setBuyQuantity(2);
        bogo.setFreeQuantity(1);
        couponRepository.save(bogo);

        Coupon vipOnly = new Coupon("PLATINUM20", DiscountType.PERCENTAGE, new BigDecimal("20"));
        vipOnly.setMinimumTier(LoyaltyTier.PLATINUM);
        vipOnly.setStackable(true);
        couponRepository.save(vipOnly);

        Coupon expired = new Coupon("LASTYEAR", DiscountType.PERCENTAGE, new BigDecimal("25"));
        expired.setValidUntil(LocalDate.now().minusMonths(1));
        couponRepository.save(expired);
    }

    private Category category(String name, TaxClass taxClass, boolean hazardous) {
        Category category = new Category(name, taxClass);
        category.setHazardous(hazardous);
        category.setDescription(name + " product category");
        return category;
    }

    private Product physical(String sku, String name, String price, String weight, Category category) {
        Product product = new Product(sku, name, new BigDecimal(price), new BigDecimal(weight), category);
        product.setDescription(name);
        return product;
    }

    private Product digital(String sku, String name, String price, Category category) {
        Product product = new Product(sku, name, new BigDecimal(price), BigDecimal.ZERO, category);
        product.setDigital(true);
        product.setDescription(name);
        return product;
    }

    private InventoryItem stock(Product product, int onHand, int reorderLevel, boolean backorderable) {
        InventoryItem item = new InventoryItem(product, onHand, reorderLevel);
        item.setBackorderable(backorderable);
        return item;
    }

    /** Small holder so the seeding steps can share the products they created. */
    private record Catalogue(Product headphones,
                             Product charger,
                             Product novel,
                             Product oliveOil,
                             Product detergent,
                             Product ide,
                             Product cameraBundle) {
    }
}
