package com.ecom.payment.dto;

import com.ecom.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResultEventDTO(
        UUID paymentId,
        UUID orderId,
        String razorpayOrderId,
        BigDecimal amount,
        PaymentStatus status,
        Long userId,
        Instant occurredAt
) {}
