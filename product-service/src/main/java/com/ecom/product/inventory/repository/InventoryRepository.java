package com.ecom.product.inventory.repository;

import com.ecom.product.inventory.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
Optional<Inventory> findBySellerIdAndProductId(Long sellerId, Long productId);
Boolean existsBySellerIdAndProductId(Long sellerId,Long productId);

    List<Inventory> findAllBySellerId(Long sellerId);

    List<Inventory> findByIdIn(List<Long> inventoryIds);

    List<Inventory> findByProduct_Id(Long productId);

    Page<Inventory> findBySellerId(Long sellerId, Pageable pageable);


    Page<Inventory> findBySellerIdOrderByCreatedOn(Long sellerId, Pageable pageable);


//    @Query("""
//SELECT new com.ecom.product.inventory.dto.SellerInventoryResponse(
//    i.id,
//    i.product.id,
//    i.availableQuantity,
//    i.reservedQuantity,
//    COALESCE(SUM(CASE WHEN l.event =
//        com.ecom.product.inventory.model.InventoryStatusEnum.CONFIRMED
//        THEN l.quantity ELSE 0 END),0),
//    COALESCE(SUM(CASE WHEN l.event =
//        com.ecom.product.inventory.model.InventoryStatusEnum.RETURNED
//        THEN l.quantity ELSE 0 END),0),
//    i.price
//)
//FROM Inventory i
//LEFT JOIN InventoryLog l ON l.inventory.id = i.id
//WHERE i.sellerId = :sellerId
//GROUP BY i.id, i.product.id, i.availableQuantity, i.reservedQuantity, i.price
//ORDER BY i.createdOn DESC
//""")
//    Page<SellerInventoryResponse> findSellerInventory(Long sellerId, Pageable pageable);
}
