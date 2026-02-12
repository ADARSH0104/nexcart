package com.ecom.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderDetailReqDTO(@NotNull Long userId,@NotNull Long inventoryId,@NotNull Long quantity) {
}
