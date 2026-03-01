package com.ecom.product.product.dto;

import com.ecom.product.product.model.Category;

import java.util.List;
import java.util.Set;

public record ProductDetailResponse(Long id, String name, String description, List<Category> productCategories, List<String> imageUrl) {
}
