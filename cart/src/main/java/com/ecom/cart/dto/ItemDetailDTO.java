package com.ecom.cart.dto;

import java.math.BigDecimal;

public record ItemDetailDTO(Long itemId, Long inventoryId, Long quantity, BigDecimal unitPrice,Boolean isOutOfStock,Boolean priceChanged,Boolean quantityChanged) {
}
