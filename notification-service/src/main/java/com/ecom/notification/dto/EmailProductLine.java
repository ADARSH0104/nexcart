package com.ecom.notification.dto;

import java.math.BigDecimal;

public record EmailProductLine(String productName, BigDecimal unitPrice,BigDecimal totalPrice, Long quantity) {}