package com.ecom.product.inventory.dto;

import java.math.BigDecimal;

public record InventoryManageDTO(Long quantity,BigDecimal price) {
}
