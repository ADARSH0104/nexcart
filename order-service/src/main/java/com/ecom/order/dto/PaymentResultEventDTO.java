package com.ecom.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResultEventDTO(
        UUID paymentId,
        UUID orderId,
        String razorpayOrderId,
        BigDecimal amount,
        PaymentStatus status,
        UUID userId,
        Instant occurredAt
) {}
