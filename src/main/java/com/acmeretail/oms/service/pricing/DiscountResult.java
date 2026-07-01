package com.acmeretail.oms.service.pricing;

import com.acmeretail.oms.domain.vo.Money;

/**
 * The combined effect of all discounts applied to an order: the automatic loyalty
 * discount, any coupon discount, whether shipping was made free, and whether the
 * total had to be capped to protect margin.
 */
public final class DiscountResult {

    private final Money loyaltyDiscount;
    private final Money couponDiscount;
    private final Money totalSubtotalDiscount;
    private final boolean freeShipping;
    private final boolean capped;
    private final String appliedCouponCode;

    public DiscountResult(Money loyaltyDiscount,
                          Money couponDiscount,
                          Money totalSubtotalDiscount,
                          boolean freeShipping,
                          boolean capped,
                          String appliedCouponCode) {
        this.loyaltyDiscount = loyaltyDiscount;
        this.couponDiscount = couponDiscount;
        this.totalSubtotalDiscount = totalSubtotalDiscount;
        this.freeShipping = freeShipping;
        this.capped = capped;
        this.appliedCouponCode = appliedCouponCode;
    }

    public static DiscountResult none(Money zero) {
        return new DiscountResult(zero, zero, zero, false, false, null);
    }

    public Money getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    public Money getCouponDiscount() {
        return couponDiscount;
    }

    public Money getTotalSubtotalDiscount() {
        return totalSubtotalDiscount;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public boolean isCapped() {
        return capped;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }
}
