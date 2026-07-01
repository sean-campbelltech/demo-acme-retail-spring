package com.acmeretail.oms.service.support;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates human-friendly, sortable order numbers of the form
 * {@code ORD-YYYYMMDD-NNNNNN}.
 */
@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AtomicLong sequence = new AtomicLong(1000L);

    public String next() {
        return next(LocalDate.now());
    }

    public String next(LocalDate date) {
        long value = sequence.incrementAndGet();
        return format(date, value);
    }

    String format(LocalDate date, long value) {
        return "ORD-" + DATE_FORMAT.format(date) + "-" + String.format("%06d", value % 1_000_000);
    }
}
