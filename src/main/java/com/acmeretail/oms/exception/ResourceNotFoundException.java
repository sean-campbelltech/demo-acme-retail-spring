package com.acmeretail.oms.exception;

/**
 * Thrown when a referenced entity (customer, product, order, ...) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, Object identifier) {
        return new ResourceNotFoundException(type + " not found: " + identifier);
    }
}
