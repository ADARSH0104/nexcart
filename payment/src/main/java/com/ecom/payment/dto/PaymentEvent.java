package com.ecom.payment.dto;

import java.util.UUID;

public record PaymentEvent(UUID orderId,PaymentEventType paymentEventType) {
}
