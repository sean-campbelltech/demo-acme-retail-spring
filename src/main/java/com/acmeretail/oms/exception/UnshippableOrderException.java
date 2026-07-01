package com.acmeretail.oms.exception;

/**
 * Thrown when an order cannot be shipped: no zone serves the destination, the
 * weight exceeds the carrier limit, or the chosen method is unavailable.
 */
public class UnshippableOrderException extends PricingException {

    public UnshippableOrderException(String message) {
        super(message);
    }
}
