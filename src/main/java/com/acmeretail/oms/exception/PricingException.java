package com.acmeretail.oms.exception;

/**
 * Base type for problems encountered while pricing an order &mdash; invalid
 * coupons, unshippable destinations, empty carts and the like.
 */
public class PricingException extends RuntimeException {

    public PricingException(String message) {
        super(message);
    }
}
