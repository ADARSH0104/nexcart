package com.ecom.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;

public record ItemRequestDTO(@NotNull Long inventoryId,@NotNull Long userId, @Positive Long quantity) {
}
