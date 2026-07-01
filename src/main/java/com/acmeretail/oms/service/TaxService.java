package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.enums.TaxClass;
import com.acmeretail.oms.domain.vo.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Calculates sales tax for a taxable amount based on the destination and the
 * product's tax class.
 *
 * <p>The rate table below is a deliberately simplified stand-in for what, in the
 * real legacy system, is a sprawling set of jurisdiction rules. It is still rich
 * enough to exercise a good number of branches.
 */
@Service
public class TaxService {

    private static final BigDecimal DEFAULT_DOMESTIC_RATE = new BigDecimal("0.0700");

    /** Region key ({@code COUNTRY} or {@code COUNTRY-REGION}) to standard rate. */
    private final Map<String, BigDecimal> standardRates = new HashMap<>();

    public TaxService() {
        standardRates.put("US-CA", new BigDecimal("0.0725"));
        standardRates.put("US-NY", new BigDecimal("0.0400"));
        standardRates.put("US-TX", new BigDecimal("0.0625"));
        standardRates.put("US-OR", new BigDecimal("0.0000"));
        standardRates.put("US-WA", new BigDecimal("0.0650"));
        standardRates.put("US-FL", new BigDecimal("0.0600"));
        standardRates.put("GB", new BigDecimal("0.2000"));
        standardRates.put("DE", new BigDecimal("0.1900"));
        standardRates.put("FR", new BigDecimal("0.2000"));
        standardRates.put("CA-ON", new BigDecimal("0.1300"));
        standardRates.put("CA-BC", new BigDecimal("0.1200"));
    }

    /**
     * Resolves the standard tax rate for a destination, falling back from the most
     * specific match ({@code COUNTRY-REGION}) to the country, and finally to the
     * default domestic rate.
     */
    public BigDecimal standardRateFor(String countryCode, String region) {
        if (countryCode == null || countryCode.isBlank()) {
            return DEFAULT_DOMESTIC_RATE;
        }
        String country = countryCode.trim().toUpperCase(Locale.ROOT);
        if (region != null && !region.isBlank()) {
            String key = country + "-" + region.trim().toUpperCase(Locale.ROOT);
            BigDecimal regionRate = standardRates.get(key);
            if (regionRate != null) {
                return regionRate;
            }
        }
        BigDecimal countryRate = standardRates.get(country);
        if (countryRate != null) {
            return countryRate;
        }
        return DEFAULT_DOMESTIC_RATE;
    }

    /**
     * Calculates the tax due on {@code taxableAmount}.
     *
     * @param taxableAmount     the amount subject to tax (already net of discounts)
     * @param taxClass          the product tax class
     * @param countryCode       destination country
     * @param region            destination region/state (may be {@code null})
     * @param customerTaxExempt whether the customer holds a tax exemption
     */
    public Money calculateTax(Money taxableAmount,
                              TaxClass taxClass,
                              String countryCode,
                              String region,
                              boolean customerTaxExempt) {
        if (taxableAmount == null) {
            throw new IllegalArgumentException("taxableAmount must not be null");
        }
        if (customerTaxExempt) {
            return Money.zero(taxableAmount.getCurrency());
        }
        if (taxClass == null || !taxClass.isTaxable()) {
            return Money.zero(taxableAmount.getCurrency());
        }
        if (!taxableAmount.isPositive()) {
            return Money.zero(taxableAmount.getCurrency());
        }

        BigDecimal rate = standardRateFor(countryCode, region);
        if (taxClass == TaxClass.REDUCED) {
            // Reduced-rate goods are taxed at half the standard rate.
            rate = rate.divide(BigDecimal.valueOf(2));
        }
        if (rate.signum() == 0) {
            return Money.zero(taxableAmount.getCurrency());
        }
        return taxableAmount.multiply(rate);
    }
}
