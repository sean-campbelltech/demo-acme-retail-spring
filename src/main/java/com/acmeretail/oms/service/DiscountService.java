package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.enums.LoyaltyTier;
import com.acmeretail.oms.domain.vo.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Encapsulates the discount rules: automatic loyalty discounts, the buy-x-get-y
 * promotion calculation, and the stacked-discount cap.
 *
 * <p>These are pure-ish calculations &mdash; they take values in and return values
 * out &mdash; which makes them an excellent target for unit-test generation.
 */
@Service
public class DiscountService {

    /**
     * Upper bound, as a percentage, on the combined effect of stacked discounts.
     * Protects margins when multiple promotions apply to the same order.
     */
    private static final BigDecimal MAX_STACKED_DISCOUNT_PERCENT = new BigDecimal("40");

    /**
     * Computes the value given away by a buy-x-get-y promotion. For every complete
     * group of {@code buyQuantity} eligible units the customer receives
     * {@code freeQuantity} units free, valued at the supplied unit price.
     */
    public Money buyXGetYDiscount(int totalEligibleUnits, int buyQuantity, int freeQuantity, Money unitPrice) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice must not be null");
        }
        if (buyQuantity <= 0 || freeQuantity <= 0 || totalEligibleUnits <= 0) {
            return Money.zero(unitPrice.getCurrency());
        }
        int groupSize = buyQuantity + freeQuantity;
        int completeGroups = totalEligibleUnits / groupSize;
        int freeUnits = completeGroups * freeQuantity;

        // Any remainder beyond the last complete group can still earn free units up
        // to freeQuantity once the buyQuantity threshold within it is reached.
        int remainder = totalEligibleUnits % groupSize;
        if (remainder > buyQuantity) {
            freeUnits += Math.min(remainder - buyQuantity, freeQuantity);
        }
        return unitPrice.multiply(freeUnits);
    }

    /**
     * The automatic, tier-based discount a customer receives on the discountable
     * subtotal regardless of coupons.
     */
    public Money loyaltyDiscount(LoyaltyTier tier, Money discountableSubtotal) {
        if (discountableSubtotal == null) {
            throw new IllegalArgumentException("discountableSubtotal must not be null");
        }
        if (tier == null || tier == LoyaltyTier.NONE || !discountableSubtotal.isPositive()) {
            return Money.zero(discountableSubtotal.getCurrency());
        }
        double rate = tier.getAutomaticDiscountRate();
        return discountableSubtotal.percentage(rate * 100.0);
    }

    /**
     * Caps a proposed total discount so that it never exceeds the configured
     * maximum percentage of the subtotal. Returns the (possibly reduced) discount.
     */
    public Money capToMaximum(Money proposedDiscount, Money subtotal) {
        if (proposedDiscount == null || subtotal == null) {
            throw new IllegalArgumentException("discount and subtotal are required");
        }
        if (MAX_STACKED_DISCOUNT_PERCENT.signum() <= 0) {
            return proposedDiscount;
        }
        Money ceiling = subtotal.percentage(MAX_STACKED_DISCOUNT_PERCENT.doubleValue());
        return proposedDiscount.isGreaterThan(ceiling) ? ceiling : proposedDiscount;
    }

    public boolean wouldBeCapped(Money proposedDiscount, Money subtotal) {
        Money capped = capToMaximum(proposedDiscount, subtotal);
        return capped.isLessThan(proposedDiscount);
    }
}
