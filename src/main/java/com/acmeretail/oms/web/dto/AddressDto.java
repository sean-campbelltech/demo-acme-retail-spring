package com.acmeretail.oms.web.dto;

import com.acmeretail.oms.domain.model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request/response representation of a postal address.
 */
public record AddressDto(
        @NotBlank @Size(max = 160) String line1,
        @Size(max = 160) String line2,
        @NotBlank @Size(max = 80) String city,
        @Size(max = 80) String region,
        @NotBlank @Size(max = 16) String postalCode,
        @NotBlank @Size(min = 2, max = 2) String countryCode) {

    public Address toAddress() {
        return new Address(line1, line2, city, region, postalCode, countryCode);
    }

    public static AddressDto from(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDto(
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountryCode());
    }
}
