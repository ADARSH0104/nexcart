package com.ecom.product.inventory.dto;

import java.math.BigDecimal;

public record InventoryDetailDTO(Long id, Long sellerId, BigDecimal price, Long quantity,Long availableQuantity) {
}
