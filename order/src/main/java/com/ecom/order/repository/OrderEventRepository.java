package com.ecom.order.repository;

import com.ecom.order.model.OrderEvent;
import com.ecom.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, UUID> {

   Boolean existsByOrderIdAndEvent_InventoryReserved(UUID orderId);
}
