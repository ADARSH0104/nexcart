package com.ecom.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCreateOrderRequestDTO(@NotNull UUID orderId,@NotNull Long userId, @NotNull @Positive BigDecimal amount) {
}
