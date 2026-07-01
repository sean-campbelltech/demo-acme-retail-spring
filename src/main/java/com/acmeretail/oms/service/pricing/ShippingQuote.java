package com.acmeretail.oms.service.pricing;

import com.acmeretail.oms.domain.enums.ShippingMethod;
import com.acmeretail.oms.domain.vo.Money;

/**
 * The outcome of a shipping calculation: the chosen method, the charge, the
 * estimated transit time and whether a free-shipping promotion zeroed the cost.
 */
public final class ShippingQuote {

    private final ShippingMethod method;
    private final Money cost;
    private final int estimatedTransitDays;
    private final boolean freeShippingApplied;

    public ShippingQuote(ShippingMethod method, Money cost, int estimatedTransitDays, boolean freeShippingApplied) {
        this.method = method;
        this.cost = cost;
        this.estimatedTransitDays = estimatedTransitDays;
        this.freeShippingApplied = freeShippingApplied;
    }

    public static ShippingQuote free(ShippingMethod method, Money zero, int estimatedTransitDays) {
        return new ShippingQuote(method, zero, estimatedTransitDays, true);
    }

    public ShippingMethod getMethod() {
        return method;
    }

    public Money getCost() {
        return cost;
    }

    public int getEstimatedTransitDays() {
        return estimatedTransitDays;
    }

    public boolean isFreeShippingApplied() {
        return freeShippingApplied;
    }
}
