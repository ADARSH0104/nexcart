package com.ecom.product.inventory.dto;

import java.math.BigDecimal;

public record InventoryDetailDTO(Long id, BigDecimal price,Long quantity) {
}
