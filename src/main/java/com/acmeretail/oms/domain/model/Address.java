package com.acmeretail.oms.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * A postal address embedded in customers and orders.
 */
@Embeddable
public class Address {

    @Column(name = "address_line1", length = 160)
    private String line1;

    @Column(name = "address_line2", length = 160)
    private String line2;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "region", length = 80)
    private String region;

    @Column(name = "postal_code", length = 16)
    private String postalCode;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    public Address() {
    }

    public Address(String line1, String line2, String city, String region, String postalCode, String countryCode) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
    }

    /**
     * An address is considered complete enough to ship to when it has a street
     * line, a city, a postal code and a country.
     */
    public boolean isComplete() {
        return hasText(line1) && hasText(city) && hasText(postalCode) && hasText(countryCode);
    }

    public boolean isDomestic(String homeCountryCode) {
        return countryCode != null && countryCode.equalsIgnoreCase(homeCountryCode);
    }

    public String getPostalPrefix() {
        if (!hasText(postalCode)) {
            return "";
        }
        String trimmed = postalCode.trim();
        return trimmed.length() <= 3 ? trimmed.toUpperCase() : trimmed.substring(0, 3).toUpperCase();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address address)) {
            return false;
        }
        return Objects.equals(line1, address.line1)
                && Objects.equals(line2, address.line2)
                && Objects.equals(city, address.city)
                && Objects.equals(region, address.region)
                && Objects.equals(postalCode, address.postalCode)
                && Objects.equals(countryCode, address.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, city, region, postalCode, countryCode);
    }
}
