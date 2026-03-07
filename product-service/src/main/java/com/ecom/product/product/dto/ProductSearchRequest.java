package com.ecom.product.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchRequest(
        String keyword,
        List<String> brands,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        String sort,
        Integer page,
        Integer size
) {}