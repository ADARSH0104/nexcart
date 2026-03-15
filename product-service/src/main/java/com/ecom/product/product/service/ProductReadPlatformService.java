package com.ecom.product.product.service;

import com.ecom.product.product.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductReadPlatformService {

    ProductDetailResponse getProductById(Long id);

    Page<ProductSearchResponse> getSearchResult(ProductSearchRequest req);

    OrderProductsSnapshot getOrderProducts(List<Long> inventoryIds);

    List<CategoryResponse> getCategoryList();

    List<ProductOptionResponse> getProductOptions();
}
