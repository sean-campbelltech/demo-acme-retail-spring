package com.acmeretail.oms.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * A shipping zone groups destinations that share pricing rules. Zones layer a
 * price multiplier and a fuel surcharge on top of the base method price, and may
 * define their own free-shipping threshold.
 */
@Entity
@Table(name = "shipping_zones")
public class ShippingZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "price_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal priceMultiplier = BigDecimal.ONE;

    @Column(name = "fuel_surcharge_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal fuelSurchargeRate = BigDecimal.ZERO;

    @Column(name = "free_shipping_threshold", precision = 12, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(name = "remote", nullable = false)
    private boolean remote;

    @Column(name = "max_weight_kg", precision = 8, scale = 3)
    private BigDecimal maxWeightKg;

    public ShippingZone() {
    }

    public ShippingZone(String code, String name, String countryCode, BigDecimal priceMultiplier) {
        this.code = code;
        this.name = name;
        this.countryCode = countryCode;
        this.priceMultiplier = priceMultiplier;
    }

    public boolean acceptsWeight(BigDecimal weightKg) {
        if (maxWeightKg == null) {
            return true;
        }
        return weightKg != null && weightKg.compareTo(maxWeightKg) <= 0;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(BigDecimal priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public BigDecimal getFuelSurchargeRate() {
        return fuelSurchargeRate;
    }

    public void setFuelSurchargeRate(BigDecimal fuelSurchargeRate) {
        this.fuelSurchargeRate = fuelSurchargeRate;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public BigDecimal getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(BigDecimal maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }
}
