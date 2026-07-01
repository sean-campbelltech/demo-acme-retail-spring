package com.acmeretail.oms.config;

import com.acmeretail.oms.domain.model.ShippingZone;
import com.acmeretail.oms.repository.ShippingZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds the in-memory database with a set of shipping zones so the shipping
 * calculation is usable the moment the application starts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ShippingZoneRepository shippingZoneRepository;

    public DataInitializer(ShippingZoneRepository shippingZoneRepository) {
        this.shippingZoneRepository = shippingZoneRepository;
    }

    @Override
    public void run(String... args) {
        if (shippingZoneRepository.count() > 0) {
            return;
        }
        seedShippingZones();
        log.info("Seeded {} shipping zones", shippingZoneRepository.count());
    }

    private void seedShippingZones() {
        shippingZoneRepository.save(zone("CA", "California & West", "US",
                "1.00", "0.0500", "75.00", false, "30.000"));
        shippingZoneRepository.save(zone("NE", "North-East", "US",
                "1.05", "0.0500", "75.00", false, "30.000"));
        shippingZoneRepository.save(remoteZone("AK", "Alaska & Remote", "US",
                "1.85", "0.0900", null, "20.000"));
        shippingZoneRepository.save(zone("GB", "United Kingdom", "GB",
                "1.60", "0.0700", "120.00", false, "25.000"));
        shippingZoneRepository.save(zone("EU", "Western Europe", "DE",
                "1.75", "0.0700", "150.00", false, "25.000"));
    }

    private ShippingZone zone(String code, String name, String country, String multiplier,
                              String fuel, String freeThreshold, boolean remote, String maxWeight) {
        ShippingZone zone = new ShippingZone(code, name, country, new BigDecimal(multiplier));
        zone.setFuelSurchargeRate(new BigDecimal(fuel));
        if (freeThreshold != null) {
            zone.setFreeShippingThreshold(new BigDecimal(freeThreshold));
        }
        zone.setRemote(remote);
        if (maxWeight != null) {
            zone.setMaxWeightKg(new BigDecimal(maxWeight));
        }
        return zone;
    }

    private ShippingZone remoteZone(String code, String name, String country, String multiplier,
                                    String fuel, String freeThreshold, String maxWeight) {
        return zone(code, name, country, multiplier, fuel, freeThreshold, true, maxWeight);
    }
}
