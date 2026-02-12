package com.ecom.product.repository;

import com.ecom.product.model.Inventory;
import com.ecom.product.model.InventoryLog;
import com.ecom.product.model.InventoryStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InventoryLogRepository extends JpaRepository<InventoryLog,Long> {
    InventoryLog findByInventoryIdAndOrderIdAndEvent(Inventory inventoryId, UUID orderId, InventoryStatusEnum inventoryStatusEnum);

    List<InventoryLog> findByEventAndExpiryAtBefore(InventoryStatusEnum event, Instant expiryAtBefore);

    Boolean existsByInventoryAndOrderIdAndEventIn(Inventory inventory, UUID orderId, List<InventoryStatusEnum> released);

    InventoryLog findByInventory_IdAndOrderIdAndEvent(Long inventoryId, UUID orderId, InventoryStatusEnum event);

    @Query(value = """
       select *
           from (
             select il.*,
                    row_number() over (
                      partition by inventory_id
                      order by created_at desc, id desc
                    ) rn
             from inventory_log il
             where il.order_id = UUID_TO_BIN(:orderId)
           ) x
           where rn = 1
    """, nativeQuery = true)
    List<InventoryLog> fetchLatestLogs(@Param("orderId")String orderId);
}
