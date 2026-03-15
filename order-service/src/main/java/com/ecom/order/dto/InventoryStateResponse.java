package com.ecom.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryStateResponse(UUID orderId, Long inventoryId, InventoryStatus status) {

}
