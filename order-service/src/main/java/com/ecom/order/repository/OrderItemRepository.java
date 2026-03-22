package com.ecom.order.repository;

import com.ecom.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrders_Id(UUID ordersId);

//    Boolean findByInventoryIdAndOrders_UserIdAndOrders_expiryAtBefore(Long inventoryId, Long ordersUserId, Instant ordersExpiryAtBefore);
//
//    Boolean findByInventoryIdAndOrders_UserIdAndOrders_expiryAtAfter(Long inventoryId, Long ordersUserId, Instant ordersExpiryAtAfter);

    Boolean existsByInventoryIdAndOrders_UserIdAndOrders_expiryAtAfter(Long inventoryId, Long ordersUserId, Instant ordersExpiryAtAfter);
}
