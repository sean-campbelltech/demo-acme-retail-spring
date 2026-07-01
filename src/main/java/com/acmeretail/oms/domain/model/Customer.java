package com.acmeretail.oms.domain.model;

import com.acmeretail.oms.domain.enums.LoyaltyTier;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_tier", nullable = false, length = 16)
    private LoyaltyTier loyaltyTier = LoyaltyTier.NONE;

    @Column(name = "lifetime_spend", nullable = false, precision = 12, scale = 2)
    private BigDecimal lifetimeSpend = BigDecimal.ZERO;

    @Column(name = "tax_exempt", nullable = false)
    private boolean taxExempt;

    @Embedded
    @AttributeOverride(name = "line1", column = @Column(name = "ship_line1"))
    @AttributeOverride(name = "line2", column = @Column(name = "ship_line2"))
    @AttributeOverride(name = "city", column = @Column(name = "ship_city"))
    @AttributeOverride(name = "region", column = @Column(name = "ship_region"))
    @AttributeOverride(name = "postalCode", column = @Column(name = "ship_postal_code"))
    @AttributeOverride(name = "countryCode", column = @Column(name = "ship_country_code"))
    private Address defaultShippingAddress = new Address();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Customer() {
    }

    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFullName() {
        StringBuilder builder = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            builder.append(firstName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(lastName.trim());
        }
        return builder.toString();
    }

    /**
     * Recomputes the loyalty tier from the customer's lifetime spend. Returns
     * {@code true} when the tier actually changed as a result.
     */
    public boolean recalculateLoyaltyTier() {
        LoyaltyTier resolved = LoyaltyTier.forLifetimeSpend(
                lifetimeSpend == null ? 0L : lifetimeSpend.longValue());
        if (resolved != this.loyaltyTier) {
            this.loyaltyTier = resolved;
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LoyaltyTier getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(LoyaltyTier loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    public BigDecimal getLifetimeSpend() {
        return lifetimeSpend;
    }

    public void setLifetimeSpend(BigDecimal lifetimeSpend) {
        this.lifetimeSpend = lifetimeSpend;
    }

    public boolean isTaxExempt() {
        return taxExempt;
    }

    public void setTaxExempt(boolean taxExempt) {
        this.taxExempt = taxExempt;
    }

    public Address getDefaultShippingAddress() {
        return defaultShippingAddress;
    }

    public void setDefaultShippingAddress(Address defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
