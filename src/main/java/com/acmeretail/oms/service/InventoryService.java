package com.acmeretail.oms.service;

import com.acmeretail.oms.domain.model.InventoryItem;
import com.acmeretail.oms.domain.model.OrderLine;
import com.acmeretail.oms.exception.InsufficientStockException;
import com.acmeretail.oms.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Checks and reserves stock for order lines.
 */
@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Number of units currently available to sell for a SKU. Unknown SKUs report
     * zero availability.
     */
    public int availableFor(String sku) {
        return inventoryItemRepository.findByProductSku(sku)
                .map(InventoryItem::availableQuantity)
                .orElse(0);
    }

    public boolean canFulfil(String sku, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        return inventoryItemRepository.findByProductSku(sku)
                .map(item -> item.canFulfil(quantity))
                .orElse(false);
    }

    /**
     * Verifies that every line on the order can be fulfilled. Throws on the first
     * line that cannot.
     */
    public void verifyAvailability(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (OrderLine line : lines) {
            if (!line.isRequiresShipping()) {
                // Digital goods are not stock-controlled.
                continue;
            }
            InventoryItem item = inventoryItemRepository.findByProductSku(line.getSku()).orElse(null);
            if (item == null || !item.canFulfil(line.getQuantity())) {
                int available = item == null ? 0 : item.availableQuantity();
                throw new InsufficientStockException(line.getSku(), line.getQuantity(), available);
            }
        }
    }

    /**
     * Reserves stock for every physical line on the order. Assumes availability has
     * already been verified.
     */
    @Transactional
    public void reserve(List<OrderLine> lines) {
        if (lines == null) {
            return;
        }
        for (OrderLine line : lines) {
            if (!line.isRequiresShipping()) {
                continue;
            }
            InventoryItem item = inventoryItemRepository.findByProductSku(line.getSku())
                    .orElseThrow(() -> new InsufficientStockException(line.getSku(), line.getQuantity(), 0));
            item.reserve(line.getQuantity());
            inventoryItemRepository.save(item);
        }
    }
}
