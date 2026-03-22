package com.ecom.order.dto;

import java.math.BigDecimal;

public record PaymentInitiateRespose(String razorpayOrderId, BigDecimal amount, String currency) {
}
