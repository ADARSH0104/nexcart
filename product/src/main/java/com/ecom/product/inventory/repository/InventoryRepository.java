package com.ecom.product.inventory.repository;

import com.ecom.product.inventory.model.Inventory;
import com.ecom.product.product.dto.ProductSnapshot;
import com.ecom.product.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
Optional<Inventory> findBySellerIdAndProductId(Long sellerId, Long productId);
Boolean existsBySellerIdAndProductId(Long sellerId,Long productId);

    List<Inventory> findByIdIn(List<Long> inventoryIds);
}
