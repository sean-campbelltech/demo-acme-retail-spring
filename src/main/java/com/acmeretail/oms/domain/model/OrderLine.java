package com.acmeretail.oms.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * A single line on an order. Price, SKU and weight are snapshotted at the time the
 * line is added so that later catalogue changes do not retroactively alter placed
 * orders.
 */
@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, length = 40)
    private String sku;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_weight_kg", nullable = false, precision = 8, scale = 3)
    private BigDecimal unitWeightKg = BigDecimal.ZERO;

    @Column(name = "discountable", nullable = false)
    private boolean discountable = true;

    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;

    public OrderLine() {
    }

    public OrderLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        if (product != null) {
            this.sku = product.getSku();
            this.description = product.getName();
            this.unitPrice = product.getUnitPrice();
            this.unitWeightKg = product.getWeightKg() != null ? product.getWeightKg() : BigDecimal.ZERO;
            this.discountable = product.isDiscountable();
            this.requiresShipping = product.requiresShipping();
        }
    }

    public BigDecimal lineSubtotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal lineWeight() {
        if (!requiresShipping || unitWeightKg == null) {
            return BigDecimal.ZERO;
        }
        return unitWeightKg.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitWeightKg() {
        return unitWeightKg;
    }

    public void setUnitWeightKg(BigDecimal unitWeightKg) {
        this.unitWeightKg = unitWeightKg;
    }

    public boolean isDiscountable() {
        return discountable;
    }

    public void setDiscountable(boolean discountable) {
        this.discountable = discountable;
    }

    public boolean isRequiresShipping() {
        return requiresShipping;
    }

    public void setRequiresShipping(boolean requiresShipping) {
        this.requiresShipping = requiresShipping;
    }
}
