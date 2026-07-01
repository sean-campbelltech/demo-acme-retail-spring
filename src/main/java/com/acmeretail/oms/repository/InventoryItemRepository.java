package com.acmeretail.oms.repository;

import com.acmeretail.oms.domain.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductId(Long productId);

    Optional<InventoryItem> findByProductSku(String sku);
}
