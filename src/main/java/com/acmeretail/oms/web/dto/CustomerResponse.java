package com.acmeretail.oms.web.dto;

import com.acmeretail.oms.domain.enums.LoyaltyTier;

import java.math.BigDecimal;

public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        LoyaltyTier loyaltyTier,
        BigDecimal lifetimeSpend,
        boolean taxExempt) {
}
