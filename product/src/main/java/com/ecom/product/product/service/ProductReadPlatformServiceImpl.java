package com.ecom.product.product.service;

import com.ecom.product.inventory.model.Inventory;
import com.ecom.product.inventory.repository.InventoryRepository;
import com.ecom.product.product.dto.*;
import com.ecom.product.product.model.Product;
import com.ecom.product.product.model.ProductImage;
import com.ecom.product.product.repository.ProductCategoryRepository;
import com.ecom.product.product.repository.ProductImageRepository;
import com.ecom.product.product.repository.ProductRepository;
import com.ecom.product.product.repository.ProductSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductReadPlatformServiceImpl implements ProductReadPlatformService{

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSearchRepository productSearchRepository;
    private final InventoryRepository inventoryRepository;

    public ProductReadPlatformServiceImpl(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ProductImageRepository productImageRepository, ProductSearchRepository productSearchRepository,InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productImageRepository = productImageRepository;
        this.productSearchRepository = productSearchRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public ProductDetailResponse getProductById(Long id) {

        Product product = this.productRepository.findById(id).get();
        List<String> imageIds = product.getProductImageSet().stream()
                .map(ProductImage::getImageUrl)
                .toList();
        ProductDetailResponse response = new ProductDetailResponse(product.getId(), product.getName(), product.getDescription(), product.getCategory().stream().toList(),imageIds.stream().toList());
        return response;
    }

    @Override
    public Page<ProductSearchResponse> getSearchResult(ProductSearchRequest req) {

        return this.productSearchRepository.search(req);
    }

    @Override
    public OrderProductsSnapshot getOrderProducts(List<Long> inventoryIds) {
        List<Inventory> inventories = this.inventoryRepository.findByIdIn(inventoryIds);
        List<ProductSnapshot> productSnapshots = new ArrayList<>();
        for(Inventory inventory :inventories){
            productSnapshots.add(new ProductSnapshot(inventory.getProduct().getId(), inventory.getProduct().getName(),inventory.getId()));
        }
        return new OrderProductsSnapshot(productSnapshots);
     }
}
