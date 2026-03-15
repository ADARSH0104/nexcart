package com.ecom.product.inventory.dto;

import java.math.BigDecimal;

public record SellerDetailDTO(Long id, Long sellerId, BigDecimal price, Long quantity) {
}
