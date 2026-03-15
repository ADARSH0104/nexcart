package com.ecom.product.inventory.dto;

import java.math.BigDecimal;

public record SellerInventoryResponse(Long inventoryId, Long productId, Long availableQuantity,Long reservedQuantity,Long soldQuantity,Long returnQuantity ,BigDecimal price) {
}
