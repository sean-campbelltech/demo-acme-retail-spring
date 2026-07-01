package com.acmeretail.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the ACME Retail Order Management System.
 *
 * <p>This service is responsible for pricing customer orders. It brings together
 * three historically separate subsystems &mdash; discounting, shipping and tax &mdash;
 * that have accreted business rules over many years of operation.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class OrderManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementSystemApplication.class, args);
    }
}
