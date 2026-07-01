package com.acmeretail.oms.web.dto;

import com.acmeretail.oms.domain.enums.ShippingMethod;

import java.math.BigDecimal;

public record PriceBreakdownResponse(
        String currencyCode,
        BigDecimal subtotal,
        BigDecimal loyaltyDiscount,
        BigDecimal couponDiscount,
        BigDecimal discountTotal,
        BigDecimal discountedSubtotal,
        BigDecimal shippingCost,
        BigDecimal tax,
        BigDecimal grandTotal,
        ShippingMethod shippingMethod,
        int estimatedTransitDays,
        boolean freeShippingApplied,
        boolean discountCapped,
        String appliedCouponCode) {
}
