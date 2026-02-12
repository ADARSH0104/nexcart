package com.ecom.order.dto.backup;

import com.ecom.order.dto.InventoryStateResponse;
import com.ecom.order.dto.InventoryStatus;

public record InventoryReservationResult (Long inventoryId, InventoryStateResponse inventoryStateResponse, InventoryStatus status) {

}
