package com.acmeretail.oms.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "warehouse_code", nullable = false, length = 12)
    private String warehouseCode = "MAIN";

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel;

    @Column(name = "backorderable", nullable = false)
    private boolean backorderable;

    public InventoryItem() {
    }

    public InventoryItem(Product product, int quantityOnHand, int reorderLevel) {
        this.product = product;
        this.quantityOnHand = quantityOnHand;
        this.reorderLevel = reorderLevel;
    }

    /**
     * Quantity that may still be sold: what is physically on hand minus what is
     * already reserved for other orders. Never negative.
     */
    public int availableQuantity() {
        int available = quantityOnHand - quantityReserved;
        return Math.max(available, 0);
    }

    public boolean canFulfil(int requestedQuantity) {
        if (requestedQuantity <= 0) {
            return false;
        }
        if (backorderable) {
            return true;
        }
        return availableQuantity() >= requestedQuantity;
    }

    public boolean isBelowReorderLevel() {
        return availableQuantity() <= reorderLevel;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("reserve quantity must be positive");
        }
        if (!canFulfil(quantity)) {
            throw new IllegalStateException("insufficient stock to reserve " + quantity
                    + " of " + (product != null ? product.getSku() : "unknown"));
        }
        this.quantityReserved += quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(int quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(int quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public boolean isBackorderable() {
        return backorderable;
    }

    public void setBackorderable(boolean backorderable) {
        this.backorderable = backorderable;
    }
}
