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
public final class Money implements Comparable<Money>, Serializable {

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

    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money zero(String currencyCode) {
        return zero(Currency.getInstance(currencyCode));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier must not be null");
        return new Money(this.amount.multiply(multiplier), currency);
    }

    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    public Money multiply(double multiplier) {
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

    public Money negate() {
        return new Money(this.amount.negate(), currency);
    }

    public Money abs() {
        return isNegative() ? negate() : this;
    }

    /**
     * Clamps this amount so that it is never below zero. Used when a discount would
     * otherwise drive a line or order total negative.
     */
    public Money clampToZero() {
        return isNegative() ? zero(currency) : this;
    }

    public Money min(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0 ? this : other;
    }

    public Money max(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0 ? this : other;
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public boolean isPositive() {
        return this.amount.signum() > 0;
    }

    public boolean isNegative() {
        return this.amount.signum() < 0;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
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
