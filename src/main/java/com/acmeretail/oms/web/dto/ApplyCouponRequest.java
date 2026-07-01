package com.acmeretail.oms.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyCouponRequest(@NotBlank String code) {
}
