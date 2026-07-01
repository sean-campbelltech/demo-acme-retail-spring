package com.acmeretail.oms.exception;

/**
 * Thrown when a coupon exists but cannot be applied to the order at hand, e.g. it
 * has expired, the minimum spend is not met, or the customer's loyalty tier is too
 * low.
 */
public class CouponNotApplicableException extends PricingException {

    private final String couponCode;
    private final String reason;

    public CouponNotApplicableException(String couponCode, String reason) {
        super("coupon '" + couponCode + "' cannot be applied: " + reason);
        this.couponCode = couponCode;
        this.reason = reason;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getReason() {
        return reason;
    }
}
