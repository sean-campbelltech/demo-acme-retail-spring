package com.acmeretail.oms.domain.model;

import com.acmeretail.oms.domain.enums.DiscountType;
import com.acmeretail.oms.domain.enums.LoyaltyTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A redeemable promotional coupon. The interpretation of {@code value} depends on
 * {@link DiscountType}:
 * <ul>
 *   <li>{@code PERCENTAGE} &ndash; percent off the discountable subtotal (0&ndash;100)</li>
 *   <li>{@code FIXED_AMOUNT} &ndash; absolute amount off the subtotal</li>
 *   <li>{@code FREE_SHIPPING} &ndash; value is ignored</li>
 *   <li>{@code BUY_X_GET_Y} &ndash; value is ignored; see buy/get quantities</li>
 * </ul>
 */
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(name = "minimum_spend", precision = 12, scale = 2)
    private BigDecimal minimumSpend;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_tier", length = 16)
    private LoyaltyTier minimumTier;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "max_redemptions")
    private Integer maxRedemptions;

    @Column(name = "times_redeemed", nullable = false)
    private int timesRedeemed;

    @Column(name = "buy_quantity")
    private Integer buyQuantity;

    @Column(name = "free_quantity")
    private Integer freeQuantity;

    @Column(name = "stackable", nullable = false)
    private boolean stackable;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Coupon() {
    }

    public Coupon(String code, DiscountType discountType, BigDecimal value) {
        this.code = code;
        this.discountType = discountType;
        this.value = value;
    }

    public boolean isWithinValidityWindow(LocalDate when) {
        if (when == null) {
            return false;
        }
        if (validFrom != null && when.isBefore(validFrom)) {
            return false;
        }
        return validUntil == null || !when.isAfter(validUntil);
    }

    public boolean hasRedemptionsRemaining() {
        return maxRedemptions == null || timesRedeemed < maxRedemptions;
    }

    public boolean meetsMinimumTier(LoyaltyTier tier) {
        if (minimumTier == null) {
            return true;
        }
        return tier != null && tier.isAtLeast(minimumTier);
    }

    public void recordRedemption() {
        this.timesRedeemed++;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMinimumSpend() {
        return minimumSpend;
    }

    public void setMinimumSpend(BigDecimal minimumSpend) {
        this.minimumSpend = minimumSpend;
    }

    public LoyaltyTier getMinimumTier() {
        return minimumTier;
    }

    public void setMinimumTier(LoyaltyTier minimumTier) {
        this.minimumTier = minimumTier;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public Integer getMaxRedemptions() {
        return maxRedemptions;
    }

    public void setMaxRedemptions(Integer maxRedemptions) {
        this.maxRedemptions = maxRedemptions;
    }

    public int getTimesRedeemed() {
        return timesRedeemed;
    }

    public void setTimesRedeemed(int timesRedeemed) {
        this.timesRedeemed = timesRedeemed;
    }

    public Integer getBuyQuantity() {
        return buyQuantity;
    }

    public void setBuyQuantity(Integer buyQuantity) {
        this.buyQuantity = buyQuantity;
    }

    public Integer getFreeQuantity() {
        return freeQuantity;
    }

    public void setFreeQuantity(Integer freeQuantity) {
        this.freeQuantity = freeQuantity;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
