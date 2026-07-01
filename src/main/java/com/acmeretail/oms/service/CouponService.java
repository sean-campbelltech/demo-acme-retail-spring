package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.enums.LoyaltyTier;
import com.acmeretail.oms.domain.model.Coupon;
import com.acmeretail.oms.domain.vo.Money;
import com.acmeretail.oms.exception.ResourceNotFoundException;
import com.acmeretail.oms.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Loads coupons and answers whether they may be applied, delegating the actual
 * rule checks to {@link DiscountService}.
 */
@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final DiscountService discountService;

    public CouponService(CouponRepository couponRepository, DiscountService discountService) {
        this.couponRepository = couponRepository;
        this.discountService = discountService;
    }

    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return couponRepository.findByCodeIgnoreCase(code.trim());
    }

    public Coupon getByCode(String code) {
        return findByCode(code)
                .orElseThrow(() -> ResourceNotFoundException.of("Coupon", code));
    }

    /**
     * Loads a coupon by code and confirms it is applicable to the supplied order
     * context, throwing otherwise.
     */
    public Coupon loadApplicableCoupon(String code,
                                       Money discountableSubtotal,
                                       LoyaltyTier tier,
                                       LocalDate when) {
        Coupon coupon = getByCode(code);
        return discountService.ensureApplicable(coupon, discountableSubtotal, tier, when);
    }

    @Transactional
    public void recordRedemption(Coupon coupon) {
        if (coupon == null) {
            return;
        }
        coupon.recordRedemption();
        couponRepository.save(coupon);
    }
}
