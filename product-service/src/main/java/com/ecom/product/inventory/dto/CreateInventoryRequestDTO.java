package com.ecom.product.inventory.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateInventoryRequestDTO(@NotNull Long productId,@NotNull BigDecimal price) {
}
