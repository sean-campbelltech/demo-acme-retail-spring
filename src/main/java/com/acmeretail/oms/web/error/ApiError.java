package com.acmeretail.oms.web.error;

import java.time.Instant;

/**
 * Standard error payload returned by the API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message);
    }
}
