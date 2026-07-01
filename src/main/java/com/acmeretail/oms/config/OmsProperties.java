package com.acmeretail.oms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Externalised business knobs for the order management system. Backed by the
 * {@code oms.*} keys in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "oms")
public class OmsProperties {

    /** ISO country code considered "domestic" for shipping and tax purposes. */
    private String homeCountryCode = "US";

    /** Currency used when an order or product does not specify one. */
    private String defaultCurrency = "USD";

    /** Order subtotal (after discounts) at or above which standard shipping is free. */
    private BigDecimal freeShippingThreshold = new BigDecimal("75.00");

    /** Flat handling fee added to every physical shipment. */
    private BigDecimal handlingFee = new BigDecimal("1.50");

    /**
     * Upper bound, as a percentage, on the combined effect of stacked discounts.
     * Protects margins when multiple promotions apply to the same order.
     */
    private BigDecimal maxStackedDiscountPercent = new BigDecimal("40");

    /** Surcharge applied to shipments containing hazardous materials. */
    private BigDecimal hazardousSurcharge = new BigDecimal("8.00");

    public String getHomeCountryCode() {
        return homeCountryCode;
    }

    public void setHomeCountryCode(String homeCountryCode) {
        this.homeCountryCode = homeCountryCode;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getHandlingFee() {
        return handlingFee;
    }

    public void setHandlingFee(BigDecimal handlingFee) {
        this.handlingFee = handlingFee;
    }

    public BigDecimal getMaxStackedDiscountPercent() {
        return maxStackedDiscountPercent;
    }

    public void setMaxStackedDiscountPercent(BigDecimal maxStackedDiscountPercent) {
        this.maxStackedDiscountPercent = maxStackedDiscountPercent;
    }

    public BigDecimal getHazardousSurcharge() {
        return hazardousSurcharge;
    }

    public void setHazardousSurcharge(BigDecimal hazardousSurcharge) {
        this.hazardousSurcharge = hazardousSurcharge;
    }
}
