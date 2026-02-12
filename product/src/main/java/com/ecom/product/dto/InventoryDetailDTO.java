package com.ecom.product.dto;

import java.math.BigDecimal;

public record InventoryDetailDTO(Long id, BigDecimal price,Long quantity) {
}
