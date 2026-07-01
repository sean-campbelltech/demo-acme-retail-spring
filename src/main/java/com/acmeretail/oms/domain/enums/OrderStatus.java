package com.acmeretail.oms.domain.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle states for an {@code Order}.
 *
 * <p>The legal transitions encoded here were reverse-engineered from the original
 * stored-procedure implementation when the system was migrated to Java.
 */
public enum OrderStatus {

    DRAFT,
    PLACED,
    PAID,
    PICKING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED;

    /**
     * Returns {@code true} when an order in this state may legally transition to
     * {@code target}.
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) {
            return false;
        }
        Set<OrderStatus> allowed = switch (this) {
            case DRAFT -> EnumSet.of(PLACED, CANCELLED);
            case PLACED -> EnumSet.of(PAID, CANCELLED);
            case PAID -> EnumSet.of(PICKING, CANCELLED, RETURNED);
            case PICKING -> EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED -> EnumSet.of(DELIVERED, RETURNED);
            case DELIVERED -> EnumSet.of(RETURNED);
            case CANCELLED, RETURNED -> EnumSet.noneOf(OrderStatus.class);
        };
        return allowed.contains(target);
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == RETURNED;
    }

    public boolean isEditable() {
        return this == DRAFT;
    }
}
