package com.acmeretail.oms.web.dto;

import com.acmeretail.oms.domain.enums.ShippingMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        ShippingMethod shippingMethod,
        @Valid AddressDto shippingAddress,
        @Valid List<OrderLineRequest> lines,
        String couponCode) {
}
