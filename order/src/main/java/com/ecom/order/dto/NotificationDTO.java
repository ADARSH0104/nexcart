package com.ecom.order.dto;

import java.util.UUID;

public record NotificationDTO(UUID orderId,UUID userId) {
}
