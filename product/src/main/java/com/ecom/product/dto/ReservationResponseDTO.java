package com.ecom.product.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponseDTO(Long inventoryId, UUID orderId, ReservationStatus status, Instant expiredAt) {
}
