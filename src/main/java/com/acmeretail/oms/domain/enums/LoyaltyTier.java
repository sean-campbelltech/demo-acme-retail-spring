package com.acmeretail.oms.domain.enums;

/**
 * Loyalty tiers earned by customers based on lifetime spend.
 *
 * <p>Each tier grants an automatic order discount and a free-shipping threshold.
 * These numbers have been tweaked repeatedly by the marketing team over the years.
 */
public enum LoyaltyTier {

    NONE(0, 0.0, null),
    BRONZE(1, 0.02, "2% member discount"),
    SILVER(2, 0.05, "5% member discount"),
    GOLD(3, 0.08, "8% member discount"),
    PLATINUM(4, 0.12, "12% member discount");

    private final int rank;
    private final double automaticDiscountRate;
    private final String marketingLabel;

    LoyaltyTier(int rank, double automaticDiscountRate, String marketingLabel) {
        this.rank = rank;
        this.automaticDiscountRate = automaticDiscountRate;
        this.marketingLabel = marketingLabel;
    }

    public int getRank() {
        return rank;
    }

    public double getAutomaticDiscountRate() {
        return automaticDiscountRate;
    }

    public String getMarketingLabel() {
        return marketingLabel;
    }

    public boolean isAtLeast(LoyaltyTier other) {
        return other != null && this.rank >= other.rank;
    }

    /**
     * Resolves the tier a customer qualifies for given their lifetime spend in
     * whole currency units.
     */
    public static LoyaltyTier forLifetimeSpend(long lifetimeSpend) {
        if (lifetimeSpend >= 25_000) {
            return PLATINUM;
        }
        if (lifetimeSpend >= 10_000) {
            return GOLD;
        }
        if (lifetimeSpend >= 2_500) {
            return SILVER;
        }
        if (lifetimeSpend >= 500) {
            return BRONZE;
        }
        return NONE;
    }
}
