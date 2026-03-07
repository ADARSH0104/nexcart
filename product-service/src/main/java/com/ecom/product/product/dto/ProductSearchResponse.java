package com.ecom.product.product.dto;

import java.math.BigDecimal;

public record ProductSearchResponse(
        Long id,
        String name,
        String brand,
        BigDecimal price,
        String thumbnailUrl,
        Boolean inStock,
        Double relevanceScore
) {}
