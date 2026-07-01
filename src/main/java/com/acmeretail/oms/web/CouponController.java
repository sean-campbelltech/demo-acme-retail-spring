package com.acmeretail.oms.web;

import com.acmeretail.oms.domain.model.Coupon;
import com.acmeretail.oms.service.CouponService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/{code}")
    public Coupon get(@PathVariable String code) {
        return couponService.getByCode(code);
    }
}
