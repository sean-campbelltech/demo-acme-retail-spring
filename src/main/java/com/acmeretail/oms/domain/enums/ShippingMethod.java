package com.acmeretail.oms.domain.enums;

/**
 * Available shipping methods and their service-level characteristics.
 *
 * <p>The base price and per-kilogram surcharge here are nationwide defaults; the
 * {@code ShippingService} layers zone multipliers and fuel surcharges on top.
 */
public enum ShippingMethod {

    ECONOMY(3.50, 0.45, 7, false),
    STANDARD(5.95, 0.60, 4, false),
    EXPRESS(12.50, 1.10, 2, true),
    OVERNIGHT(24.00, 1.85, 1, true),
    PICKUP(0.00, 0.00, 0, false);

    private final double basePrice;
    private final double perKilogramSurcharge;
    private final int estimatedTransitDays;
    private final boolean expedited;

    ShippingMethod(double basePrice, double perKilogramSurcharge, int estimatedTransitDays, boolean expedited) {
        this.basePrice = basePrice;
        this.perKilogramSurcharge = perKilogramSurcharge;
        this.estimatedTransitDays = estimatedTransitDays;
        this.expedited = expedited;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getPerKilogramSurcharge() {
        return perKilogramSurcharge;
    }

    public int getEstimatedTransitDays() {
        return estimatedTransitDays;
    }

    public boolean isExpedited() {
        return expedited;
    }

    public boolean requiresSignature() {
        return this == EXPRESS || this == OVERNIGHT;
    }
}
