package com.acmeretail.oms.service;

import com.acmeretail.oms.config.OmsProperties;
import com.acmeretail.oms.domain.enums.DiscountType;
import com.acmeretail.oms.domain.enums.LoyaltyTier;
import com.acmeretail.oms.domain.model.Coupon;
import com.acmeretail.oms.domain.vo.Money;
import com.acmeretail.oms.exception.CouponNotApplicableException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encapsulates the discount rules: automatic loyalty discounts, coupon validation,
 * the various coupon discount calculations, and the stacked-discount cap.
 *
 * <p>These are pure-ish calculations &mdash; they take values in and return values
 * out &mdash; which makes them an excellent target for unit-test generation.
 */
@Service
public class DiscountService {

    private final OmsProperties properties;

    public DiscountService(OmsProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns a human-readable reason why {@code coupon} cannot be applied, or
     * {@code null} when it is applicable.
     */
    public String applicabilityProblem(Coupon coupon,
                                       Money discountableSubtotal,
                                       LoyaltyTier tier,
                                       LocalDate when) {
        if (coupon == null) {
            return "coupon does not exist";
        }
        if (!coupon.isActive()) {
            return "coupon is not active";
        }
        if (!coupon.isWithinValidityWindow(when)) {
            return "coupon is outside its validity window";
        }
        if (!coupon.hasRedemptionsRemaining()) {
            return "coupon has no redemptions remaining";
        }
        if (!coupon.meetsMinimumTier(tier)) {
            return "customer loyalty tier is too low for this coupon";
        }
        if (coupon.getMinimumSpend() != null) {
            Money minimum = Money.of(coupon.getMinimumSpend(), discountableSubtotal.getCurrency());
            if (discountableSubtotal.isLessThan(minimum)) {
                return "order does not meet the minimum spend of " + minimum;
            }
        }
        if (coupon.getDiscountType() == DiscountType.BUY_X_GET_Y
                && (coupon.getBuyQuantity() == null || coupon.getFreeQuantity() == null)) {
            return "buy-x-get-y coupon is misconfigured";
        }
        return null;
    }

    public boolean isApplicable(Coupon coupon, Money discountableSubtotal, LoyaltyTier tier, LocalDate when) {
        return applicabilityProblem(coupon, discountableSubtotal, tier, when) == null;
    }

    /**
     * Throws when the coupon is not applicable; otherwise returns it unchanged.
     */
    public Coupon ensureApplicable(Coupon coupon, Money discountableSubtotal, LoyaltyTier tier, LocalDate when) {
        String problem = applicabilityProblem(coupon, discountableSubtotal, tier, when);
        if (problem != null) {
            String code = coupon != null ? coupon.getCode() : "<unknown>";
            throw new CouponNotApplicableException(code, problem);
        }
        return coupon;
    }

    /**
     * The discount a {@code PERCENTAGE} or {@code FIXED_AMOUNT} coupon takes off the
     * discountable subtotal. Never exceeds the subtotal itself.
     */
    public Money couponSubtotalDiscount(Coupon coupon, Money discountableSubtotal) {
        if (coupon == null || discountableSubtotal == null) {
            throw new IllegalArgumentException("coupon and subtotal are required");
        }
        Money discount;
        switch (coupon.getDiscountType()) {
            case PERCENTAGE -> {
                double percent = coupon.getValue() == null ? 0d : coupon.getValue().doubleValue();
                discount = discountableSubtotal.percentage(percent);
            }
            case FIXED_AMOUNT -> {
                BigDecimal value = coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue();
                discount = Money.of(value, discountableSubtotal.getCurrency());
            }
            default -> discount = Money.zero(discountableSubtotal.getCurrency());
        }
        // A coupon can never discount more than the discountable subtotal.
        return discount.min(discountableSubtotal).clampToZero();
    }

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
        BigDecimal maxPercent = properties.getMaxStackedDiscountPercent();
        if (maxPercent == null || maxPercent.signum() <= 0) {
            return proposedDiscount;
        }
        Money ceiling = subtotal.percentage(maxPercent.doubleValue());
        return proposedDiscount.isGreaterThan(ceiling) ? ceiling : proposedDiscount;
    }

    public boolean wouldBeCapped(Money proposedDiscount, Money subtotal) {
        Money capped = capToMaximum(proposedDiscount, subtotal);
        return capped.isLessThan(proposedDiscount);
    }
}
