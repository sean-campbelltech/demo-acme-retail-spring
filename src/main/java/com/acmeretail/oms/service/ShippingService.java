package com.acmeretail.oms.service;

import com.acmeretail.oms.config.OmsProperties;
import com.acmeretail.oms.domain.enums.ShippingMethod;
import com.acmeretail.oms.domain.model.ShippingZone;
import com.acmeretail.oms.domain.vo.Money;
import com.acmeretail.oms.exception.UnshippableOrderException;
import com.acmeretail.oms.repository.ShippingZoneRepository;
import com.acmeretail.oms.service.pricing.ShippingQuote;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Works out how much it costs to ship an order.
 *
 * <p>The charge is built up from several layers that have accumulated over the
 * life of the system:
 * <ol>
 *   <li>a base price plus a per-kilogram surcharge for the chosen method;</li>
 *   <li>a destination zone multiplier;</li>
 *   <li>a fuel surcharge proportional to the zone-adjusted carriage;</li>
 *   <li>a flat handling fee, plus a hazardous-goods surcharge where relevant;</li>
 *   <li>finally, free-shipping promotions for qualifying standard shipments.</li>
 * </ol>
 */
@Service
public class ShippingService {

    private final ShippingZoneRepository shippingZoneRepository;
    private final OmsProperties properties;

    public ShippingService(ShippingZoneRepository shippingZoneRepository, OmsProperties properties) {
        this.shippingZoneRepository = shippingZoneRepository;
        this.properties = properties;
    }

    /**
     * Calculates a shipping quote.
     *
     * @param method            the requested shipping method
     * @param totalWeightKg     total billable weight of physical goods
     * @param discountedSubtotal order subtotal after discounts (drives free shipping)
     * @param countryCode       destination country
     * @param region            destination region/state (may be {@code null})
     * @param hasPhysicalGoods  whether the order contains anything that ships
     * @param hazardous         whether the shipment contains hazardous materials
     */
    public ShippingQuote quote(ShippingMethod method,
                               BigDecimal totalWeightKg,
                               Money discountedSubtotal,
                               String countryCode,
                               String region,
                               boolean hasPhysicalGoods,
                               boolean hazardous) {
        if (method == null) {
            throw new IllegalArgumentException("shipping method must not be null");
        }
        if (discountedSubtotal == null) {
            throw new IllegalArgumentException("discountedSubtotal must not be null");
        }

        // Nothing physical to ship (digital-only order) or in-store pickup: no charge.
        if (!hasPhysicalGoods || method == ShippingMethod.PICKUP) {
            return new ShippingQuote(method, Money.zero(discountedSubtotal.getCurrency()),
                    method.getEstimatedTransitDays(), false);
        }

        BigDecimal weight = totalWeightKg == null ? BigDecimal.ZERO : totalWeightKg;
        if (weight.signum() < 0) {
            throw new IllegalArgumentException("totalWeightKg must not be negative");
        }

        ShippingZone zone = resolveZone(countryCode, region);
        if (!zone.acceptsWeight(weight)) {
            throw new UnshippableOrderException("shipment weight " + weight
                    + "kg exceeds the limit for zone " + zone.getCode());
        }
        if (zone.isRemote() && !method.isExpedited() && method != ShippingMethod.STANDARD) {
            throw new UnshippableOrderException("method " + method
                    + " is not available to remote zone " + zone.getCode());
        }

        // Layer 1: base + per-kg carriage.
        Money carriage = Money.of(BigDecimal.valueOf(method.getBasePrice()), discountedSubtotal.getCurrency())
                .add(Money.of(BigDecimal.valueOf(method.getPerKilogramSurcharge()), discountedSubtotal.getCurrency())
                        .multiply(weight));

        // Layer 2: zone multiplier.
        carriage = carriage.multiply(zone.getPriceMultiplier());

        // Layer 3: fuel surcharge on the zone-adjusted carriage.
        Money fuel = carriage.multiply(zone.getFuelSurchargeRate());
        Money cost = carriage.add(fuel);

        // Layer 4: handling fee and hazardous surcharge.
        cost = cost.add(Money.of(properties.getHandlingFee(), discountedSubtotal.getCurrency()));
        if (hazardous) {
            cost = cost.add(Money.of(properties.getHazardousSurcharge(), discountedSubtotal.getCurrency()));
        }

        // Layer 5: free-shipping promotions only apply to non-expedited methods.
        if (!method.isExpedited() && qualifiesForFreeShipping(discountedSubtotal, zone)) {
            return ShippingQuote.free(method, Money.zero(discountedSubtotal.getCurrency()),
                    method.getEstimatedTransitDays());
        }

        return new ShippingQuote(method, cost, method.getEstimatedTransitDays(), false);
    }

    private boolean qualifiesForFreeShipping(Money discountedSubtotal, ShippingZone zone) {
        BigDecimal threshold = zone.getFreeShippingThreshold() != null
                ? zone.getFreeShippingThreshold()
                : properties.getFreeShippingThreshold();
        if (threshold == null) {
            return false;
        }
        Money thresholdMoney = Money.of(threshold, discountedSubtotal.getCurrency());
        return discountedSubtotal.isGreaterThanOrEqualTo(thresholdMoney);
    }

    /**
     * Finds the shipping zone serving a destination. Domestic destinations without
     * an explicit zone fall back to a default zone; foreign destinations without a
     * zone are rejected as unshippable.
     */
    ShippingZone resolveZone(String countryCode, String region) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new UnshippableOrderException("destination country is required");
        }
        String country = countryCode.trim().toUpperCase(Locale.ROOT);
        List<ShippingZone> candidates = shippingZoneRepository.findByCountryCode(country);
        if (candidates.isEmpty()) {
            if (country.equalsIgnoreCase(properties.getHomeCountryCode())) {
                return defaultDomesticZone(country);
            }
            throw new UnshippableOrderException("no shipping zone serves country " + country);
        }
        if (region != null && !region.isBlank()) {
            String prefix = region.trim().toUpperCase(Locale.ROOT);
            for (ShippingZone candidate : candidates) {
                if (prefix.equals(candidate.getCode())) {
                    return candidate;
                }
            }
        }
        return candidates.get(0);
    }

    private ShippingZone defaultDomesticZone(String country) {
        ShippingZone zone = new ShippingZone("DOMESTIC", "Domestic", country, BigDecimal.ONE);
        zone.setFuelSurchargeRate(new BigDecimal("0.0500"));
        return zone;
    }
}
