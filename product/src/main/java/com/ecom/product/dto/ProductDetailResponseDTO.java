package com.ecom.product.dto;

import com.ecom.product.model.Category;

import java.util.Set;

public record ProductDetailResponseDTO(Long id, String name, String description, Set<Category> productCategories, Set<Long> imageId) {
}
