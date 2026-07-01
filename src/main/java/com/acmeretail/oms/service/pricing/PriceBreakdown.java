package com.acmeretail.oms.service.pricing;

import com.acmeretail.oms.domain.enums.ShippingMethod;
import com.acmeretail.oms.domain.vo.Money;

/**
 * The fully itemised result of pricing an order. Every figure is expressed in the
 * order's currency.
 */
public final class PriceBreakdown {

    private final Money subtotal;
    private final Money loyaltyDiscount;
    private final Money couponDiscount;
    private final Money discountTotal;
    private final Money discountedSubtotal;
    private final Money shippingCost;
    private final Money tax;
    private final Money grandTotal;
    private final ShippingMethod shippingMethod;
    private final int estimatedTransitDays;
    private final boolean freeShippingApplied;
    private final boolean discountCapped;
    private final String appliedCouponCode;

    private PriceBreakdown(Builder builder) {
        this.subtotal = builder.subtotal;
        this.loyaltyDiscount = builder.loyaltyDiscount;
        this.couponDiscount = builder.couponDiscount;
        this.discountTotal = builder.discountTotal;
        this.discountedSubtotal = builder.discountedSubtotal;
        this.shippingCost = builder.shippingCost;
        this.tax = builder.tax;
        this.grandTotal = builder.grandTotal;
        this.shippingMethod = builder.shippingMethod;
        this.estimatedTransitDays = builder.estimatedTransitDays;
        this.freeShippingApplied = builder.freeShippingApplied;
        this.discountCapped = builder.discountCapped;
        this.appliedCouponCode = builder.appliedCouponCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    public Money getCouponDiscount() {
        return couponDiscount;
    }

    public Money getDiscountTotal() {
        return discountTotal;
    }

    public Money getDiscountedSubtotal() {
        return discountedSubtotal;
    }

    public Money getShippingCost() {
        return shippingCost;
    }

    public Money getTax() {
        return tax;
    }

    public Money getGrandTotal() {
        return grandTotal;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

    public int getEstimatedTransitDays() {
        return estimatedTransitDays;
    }

    public boolean isFreeShippingApplied() {
        return freeShippingApplied;
    }

    public boolean isDiscountCapped() {
        return discountCapped;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public static final class Builder {
        private Money subtotal;
        private Money loyaltyDiscount;
        private Money couponDiscount;
        private Money discountTotal;
        private Money discountedSubtotal;
        private Money shippingCost;
        private Money tax;
        private Money grandTotal;
        private ShippingMethod shippingMethod;
        private int estimatedTransitDays;
        private boolean freeShippingApplied;
        private boolean discountCapped;
        private String appliedCouponCode;

        public Builder subtotal(Money value) {
            this.subtotal = value;
            return this;
        }

        public Builder loyaltyDiscount(Money value) {
            this.loyaltyDiscount = value;
            return this;
        }

        public Builder couponDiscount(Money value) {
            this.couponDiscount = value;
            return this;
        }

        public Builder discountTotal(Money value) {
            this.discountTotal = value;
            return this;
        }

        public Builder discountedSubtotal(Money value) {
            this.discountedSubtotal = value;
            return this;
        }

        public Builder shippingCost(Money value) {
            this.shippingCost = value;
            return this;
        }

        public Builder tax(Money value) {
            this.tax = value;
            return this;
        }

        public Builder grandTotal(Money value) {
            this.grandTotal = value;
            return this;
        }

        public Builder shippingMethod(ShippingMethod value) {
            this.shippingMethod = value;
            return this;
        }

        public Builder estimatedTransitDays(int value) {
            this.estimatedTransitDays = value;
            return this;
        }

        public Builder freeShippingApplied(boolean value) {
            this.freeShippingApplied = value;
            return this;
        }

        public Builder discountCapped(boolean value) {
            this.discountCapped = value;
            return this;
        }

        public Builder appliedCouponCode(String value) {
            this.appliedCouponCode = value;
            return this;
        }

        public PriceBreakdown build() {
            return new PriceBreakdown(this);
        }
    }
}
