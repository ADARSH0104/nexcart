package com.ecom.cart.dto;

import java.math.BigDecimal;

public record InventoryDetailDTO(Long id, BigDecimal price, Long quantity) {
}
