package com.ecom.product.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record InventoryRequestDTO(@NotNull UUID orderId,   @NotNull @Positive Long quantity ) {
}
