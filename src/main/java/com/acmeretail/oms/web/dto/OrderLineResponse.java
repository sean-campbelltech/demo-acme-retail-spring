package com.acmeretail.oms.web.dto;

import java.math.BigDecimal;

public record OrderLineResponse(
        String sku,
        String description,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineSubtotal) {
}
