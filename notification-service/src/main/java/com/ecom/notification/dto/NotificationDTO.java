package com.ecom.notification.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record NotificationDTO(UUID orderId, UUID userId, BigDecimal totalAmount, List<OrderItemDetailsDTO> orderDetails) {
}
