package com.ecom.product.product.dto;

import com.ecom.product.inventory.dto.SellerDetailDTO;
import com.ecom.product.product.model.Category;

import java.util.List;

public record ProductDetailResponse(Long id, String name, String description, List<Category> productCategories, List<String> imageUrl, List<SellerDetailDTO> sellerDetails) {
}
