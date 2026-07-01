package com.acmeretail.oms.service;

import com.acmeretail.oms.config.OmsProperties;
import com.acmeretail.oms.domain.enums.DiscountType;
import com.acmeretail.oms.domain.enums.LoyaltyTier;
import com.acmeretail.oms.domain.enums.TaxClass;
import com.acmeretail.oms.domain.model.Coupon;
import com.acmeretail.oms.domain.model.Customer;
import com.acmeretail.oms.domain.model.Order;
import com.acmeretail.oms.domain.model.OrderLine;
import com.acmeretail.oms.domain.model.Product;
import com.acmeretail.oms.domain.vo.Money;
import com.acmeretail.oms.exception.PricingException;
import com.acmeretail.oms.service.pricing.PriceBreakdown;
import com.acmeretail.oms.service.pricing.ShippingQuote;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;

/**
 * The heart of the system: turns an order into a fully itemised
 * {@link PriceBreakdown} by orchestrating the discount, shipping and tax services.
 *
 * <p>The ordering of operations here matters and is the source of much of the
 * system's accumulated complexity: discounts are applied to the subtotal first,
 * the discounted subtotal then drives both shipping (free-shipping thresholds) and
 * the taxable base, and tax is finally computed line by line so that differing tax
 * classes are respected.
 */
@Service
public class PricingService {

    private final DiscountService discountService;
    private final ShippingService shippingService;
    private final TaxService taxService;
    private final CouponService couponService;
    private final OmsProperties properties;

    public PricingService(DiscountService discountService,
                          ShippingService shippingService,
                          TaxService taxService,
                          CouponService couponService,
                          OmsProperties properties) {
        this.discountService = discountService;
        this.shippingService = shippingService;
        this.taxService = taxService;
        this.couponService = couponService;
        this.properties = properties;
    }

    public PriceBreakdown priceOrder(Order order) {
        return priceOrder(order, LocalDate.now());
    }

    public PriceBreakdown priceOrder(Order order, LocalDate when) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        if (order.isEmpty()) {
            throw new PricingException("cannot price an order with no lines");
        }

        Currency currency = resolveCurrency(order);
        Money zero = Money.zero(currency);

        Money subtotal = zero;
        Money discountableSubtotal = zero;
        for (OrderLine line : order.getLines()) {
            Money lineTotal = Money.of(line.lineSubtotal(), currency);
            subtotal = subtotal.add(lineTotal);
            if (line.isDiscountable()) {
                discountableSubtotal = discountableSubtotal.add(lineTotal);
            }
        }

        Customer customer = order.getCustomer();
        LoyaltyTier tier = customer != null ? customer.getLoyaltyTier() : LoyaltyTier.NONE;

        Money loyaltyDiscount = discountService.loyaltyDiscount(tier, discountableSubtotal);

        Money couponDiscount = zero;
        boolean couponFreeShipping = false;
        String appliedCouponCode = null;
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            Coupon coupon = couponService.loadApplicableCoupon(
                    order.getCouponCode(), discountableSubtotal, tier, when);
            appliedCouponCode = coupon.getCode();
            DiscountType type = coupon.getDiscountType();
            if (type == DiscountType.FREE_SHIPPING) {
                couponFreeShipping = true;
            } else if (type == DiscountType.BUY_X_GET_Y) {
                couponDiscount = buyXGetY(order, coupon, currency);
            } else {
                couponDiscount = discountService.couponSubtotalDiscount(coupon, discountableSubtotal);
            }
        }

        Money proposedDiscount = loyaltyDiscount.add(couponDiscount);
        boolean capped = discountService.wouldBeCapped(proposedDiscount, subtotal);
        Money discountTotal = discountService.capToMaximum(proposedDiscount, subtotal);
        Money discountedSubtotal = subtotal.subtract(discountTotal).clampToZero();

        ShippingQuote shipping = quoteShipping(order, discountedSubtotal, couponFreeShipping);
        Money shippingCost = couponFreeShipping ? zero : shipping.getCost();
        boolean freeShippingApplied = couponFreeShipping || shipping.isFreeShippingApplied();

        Money tax = calculateTax(order, subtotal, discountTotal, currency);

        Money grandTotal = discountedSubtotal.add(shippingCost).add(tax);

        return PriceBreakdown.builder()
                .subtotal(subtotal)
                .loyaltyDiscount(loyaltyDiscount)
                .couponDiscount(couponDiscount)
                .discountTotal(discountTotal)
                .discountedSubtotal(discountedSubtotal)
                .shippingCost(shippingCost)
                .tax(tax)
                .grandTotal(grandTotal)
                .shippingMethod(order.getShippingMethod())
                .estimatedTransitDays(shipping.getEstimatedTransitDays())
                .freeShippingApplied(freeShippingApplied)
                .discountCapped(capped)
                .appliedCouponCode(appliedCouponCode)
                .build();
    }

    private ShippingQuote quoteShipping(Order order, Money discountedSubtotal, boolean couponFreeShipping) {
        String country = countryFor(order);
        String region = regionFor(order);
        ShippingQuote quote = shippingService.quote(
                order.getShippingMethod(),
                order.totalWeight(),
                discountedSubtotal,
                country,
                region,
                order.hasPhysicalGoods(),
                containsHazardousGoods(order));
        return quote;
    }

    private Money buyXGetY(Order order, Coupon coupon, Currency currency) {
        int eligibleUnits = 0;
        Money cheapest = null;
        for (OrderLine line : order.getLines()) {
            if (!line.isDiscountable()) {
                continue;
            }
            eligibleUnits += line.getQuantity();
            Money unit = Money.of(line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice(), currency);
            cheapest = (cheapest == null) ? unit : cheapest.min(unit);
        }
        if (cheapest == null) {
            return Money.zero(currency);
        }
        return discountService.buyXGetYDiscount(
                eligibleUnits, coupon.getBuyQuantity(), coupon.getFreeQuantity(), cheapest);
    }

    /**
     * Computes tax line by line. The order-level discount is allocated to each line
     * in proportion to its share of the subtotal, so the taxable base reflects what
     * the customer actually pays for that line.
     */
    private Money calculateTax(Order order, Money subtotal, Money discountTotal, Currency currency) {
        Money tax = Money.zero(currency);
        if (!subtotal.isPositive()) {
            return tax;
        }
        Customer customer = order.getCustomer();
        boolean taxExempt = customer != null && customer.isTaxExempt();
        String country = countryFor(order);
        String region = regionFor(order);

        for (OrderLine line : order.getLines()) {
            Money lineTotal = Money.of(line.lineSubtotal(), currency);
            if (!lineTotal.isPositive()) {
                continue;
            }
            BigDecimal share = lineTotal.getAmount()
                    .divide(subtotal.getAmount(), 10, RoundingMode.HALF_UP);
            Money lineDiscount = discountTotal.multiply(share);
            Money taxableBase = lineTotal.subtract(lineDiscount).clampToZero();

            TaxClass taxClass = resolveTaxClass(line);
            tax = tax.add(taxService.calculateTax(taxableBase, taxClass, country, region, taxExempt));
        }
        return tax;
    }

    private TaxClass resolveTaxClass(OrderLine line) {
        Product product = line.getProduct();
        return product != null ? product.resolveTaxClass() : TaxClass.STANDARD;
    }

    private boolean containsHazardousGoods(Order order) {
        for (OrderLine line : order.getLines()) {
            Product product = line.getProduct();
            if (product != null && product.getCategory() != null && product.getCategory().isHazardous()) {
                return true;
            }
        }
        return false;
    }

    private String countryFor(Order order) {
        if (order.getShippingAddress() != null && order.getShippingAddress().getCountryCode() != null) {
            return order.getShippingAddress().getCountryCode();
        }
        Customer customer = order.getCustomer();
        if (customer != null && customer.getDefaultShippingAddress() != null) {
            return customer.getDefaultShippingAddress().getCountryCode();
        }
        return properties.getHomeCountryCode();
    }

    private String regionFor(Order order) {
        if (order.getShippingAddress() != null && order.getShippingAddress().getRegion() != null) {
            return order.getShippingAddress().getRegion();
        }
        Customer customer = order.getCustomer();
        if (customer != null && customer.getDefaultShippingAddress() != null) {
            return customer.getDefaultShippingAddress().getRegion();
        }
        return null;
    }

    private Currency resolveCurrency(Order order) {
        String code = order.getCurrencyCode() != null ? order.getCurrencyCode() : properties.getDefaultCurrency();
        return Currency.getInstance(code);
    }
}
