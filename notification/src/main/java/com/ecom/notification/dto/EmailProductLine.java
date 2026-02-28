package com.ecom.notification.dto;

import java.math.BigDecimal;

public record EmailProductLine(String productName, BigDecimal price, Long quantity) {}