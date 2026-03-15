package com.ecom.order.dto;

import java.math.BigDecimal;

public record PaymentCreateOrderResponseDTO(String razorpayOrderId,String status, BigDecimal amount) {
}
