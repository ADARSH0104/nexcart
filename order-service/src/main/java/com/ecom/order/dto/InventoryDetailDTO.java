package com.ecom.order.dto;

import java.math.BigDecimal;

public record InventoryDetailDTO(Long id, BigDecimal price,Long quantity) {
}
