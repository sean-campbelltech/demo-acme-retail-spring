package com.acmeretail.oms.web.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String sku,
        String name,
        BigDecimal unitPrice,
        String currencyCode,
        BigDecimal weightKg,
        String category,
        boolean digital,
        boolean active) {
}
