package com.acmeretail.oms.domain.enums;

/**
 * The kinds of discount a coupon or promotion can apply.
 */
public enum DiscountType {

    /** A percentage off the order subtotal, e.g. 15% off. */
    PERCENTAGE,

    /** A fixed monetary amount off the order subtotal, e.g. $10 off. */
    FIXED_AMOUNT,

    /** Removes shipping charges entirely. */
    FREE_SHIPPING,

    /** Buy a qualifying quantity, get further units free. */
    BUY_X_GET_Y;

    public boolean affectsSubtotal() {
        return this == PERCENTAGE || this == FIXED_AMOUNT || this == BUY_X_GET_Y;
    }

    public boolean affectsShipping() {
        return this == FREE_SHIPPING;
    }
}
