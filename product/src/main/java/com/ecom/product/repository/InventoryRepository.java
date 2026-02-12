package com.ecom.product.repository;

import com.ecom.product.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
Optional<Inventory> findBySellerIdAndProductId(Long sellerId, Long productId);
Boolean existsBySellerIdAndProductId(Long sellerId,Long productId);

}
