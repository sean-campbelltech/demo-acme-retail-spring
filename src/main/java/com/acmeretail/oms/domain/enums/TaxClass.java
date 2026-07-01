package com.acmeretail.oms.domain.enums;

/**
 * Tax treatment for a category of products. The actual rate applied also depends
 * on the destination region, which is resolved by the {@code TaxService}.
 */
public enum TaxClass {

    /** Ordinary goods taxed at the destination's full rate. */
    STANDARD,

    /** Goods taxed at a reduced rate (e.g. some household staples). */
    REDUCED,

    /** Zero-rated goods (e.g. most groceries and books in many jurisdictions). */
    ZERO_RATED,

    /** Goods that are entirely exempt from sales tax. */
    EXEMPT;

    public boolean isTaxable() {
        return this == STANDARD || this == REDUCED;
    }
}
