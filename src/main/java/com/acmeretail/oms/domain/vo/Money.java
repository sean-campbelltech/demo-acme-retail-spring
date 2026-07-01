package com.acmeretail.oms.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An immutable monetary amount in a specific currency.
 *
 * <p>All arithmetic is performed with {@link BigDecimal} and rounded to the
 * currency's default fraction digits using {@link RoundingMode#HALF_UP}. Operations
 * that combine two {@code Money} values require matching currencies and will throw
 * an {@link IllegalArgumentException} otherwise.
 *
 * <p>This class is deliberately self-contained &mdash; it has no Spring or JPA
 * dependencies &mdash; which makes it a good candidate for fine-grained unit tests.
 */
public final class Money implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        this.currency = currency;
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier must not be null");
        return new Money(this.amount.multiply(multiplier), currency);
    }

    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Returns the given percentage of this amount. A {@code percentage} of 15 means
     * "15 percent of". Negative percentages are rejected.
     */
    public Money percentage(double percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("percentage must not be negative: " + percentage);
        }
        BigDecimal factor = BigDecimal.valueOf(percentage).movePointLeft(2);
        return new Money(this.amount.multiply(factor), currency);
    }

    public boolean isPositive() {
        return this.amount.signum() > 0;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "currency mismatch: " + this.currency.getCurrencyCode()
                            + " vs " + other.currency.getCurrencyCode());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
