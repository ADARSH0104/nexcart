package com.ecom.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderDetailReqDTO(@NotNull UUID userId, @NotNull Long inventoryId, @NotNull Long quantity) {
}
