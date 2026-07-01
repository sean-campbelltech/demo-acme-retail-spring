package com.acmeretail.oms.repository;

import com.acmeretail.oms.domain.model.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingZoneRepository extends JpaRepository<ShippingZone, Long> {

    Optional<ShippingZone> findByCode(String code);

    List<ShippingZone> findByCountryCode(String countryCode);
}
