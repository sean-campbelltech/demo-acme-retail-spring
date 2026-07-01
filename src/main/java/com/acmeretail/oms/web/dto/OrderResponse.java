package com.acmeretail.oms.web.dto;

import com.acmeretail.oms.domain.enums.OrderStatus;
import com.acmeretail.oms.domain.enums.ShippingMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long customerId,
        OrderStatus status,
        ShippingMethod shippingMethod,
        String couponCode,
        String currencyCode,
        List<OrderLineResponse> lines,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal shippingTotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal) {
}
