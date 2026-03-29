package com.ecom.product.product.service;

import com.ecom.product.inventory.dto.SellerDetailDTO;
import com.ecom.product.inventory.model.Inventory;
import com.ecom.product.inventory.repository.InventoryRepository;
import com.ecom.product.inventory.service.InventoryReadPlatformService;
import com.ecom.product.product.dto.*;
import com.ecom.product.product.model.Product;
import com.ecom.product.product.model.ProductImage;
import com.ecom.product.product.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductReadPlatformServiceImpl implements ProductReadPlatformService{

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSearchRepository productSearchRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryReadPlatformService inventoryReadPlatformService;

    public ProductReadPlatformServiceImpl(ProductRepository productRepository,
                                          ProductCategoryRepository productCategoryRepository,
                                          ProductImageRepository productImageRepository,
                                          ProductSearchRepository productSearchRepository,
                                          InventoryRepository inventoryRepository,
                                          CategoryRepository categoryRepository,
                                          InventoryReadPlatformService inventoryReadPlatformService) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productImageRepository = productImageRepository;
        this.productSearchRepository = productSearchRepository;
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryReadPlatformService = inventoryReadPlatformService;
    }

    @Override
    public ProductDetailResponse getProductById(Long id) {

        Product product = this.productRepository.findById(id).get();
        List<String> imageIds = product.getProductImageSet().stream()
                .map(ProductImage::getImageUrl)
                .toList();

        List<SellerDetailDTO> sellerDetails = this.inventoryReadPlatformService.getSellerDetails(id);
        ProductDetailResponse response = new ProductDetailResponse(product.getId(), product.getName(), product.getDescription(), product.getCategory().stream().toList(),imageIds.stream().toList(),sellerDetails);
        return response;
    }

    @Override
    public Page<ProductSearchResponse> getSearchResult(ProductSearchRequest req) {

        return this.productSearchRepository.search(req);
    }

    @Override
    public OrderProductsSnapshot getOrderProducts(List<Long> inventoryIds) {
        List<Inventory> inventories = this.inventoryRepository.findByIds(inventoryIds);
        Map<Long,ProductSnapshot> productSnapshots = new HashMap<>();
        for(Inventory inventory :inventories){
            Product product = inventory.getProduct();
            productSnapshots.put(inventory.getId(),new ProductSnapshot(product.getId(), product.getName(),product.getThumbnailUrl()));
        }
        return new OrderProductsSnapshot(productSnapshots);
     }

    @Override
    public List<CategoryResponse> getCategoryList() {
        List<CategoryResponse> categoryList = this.categoryRepository.findAll()
                .stream()
                .map((c)->{ return new CategoryResponse(c.getId(), c.getName());})
                .toList();
        return categoryList;
    }


    @Override
    public List<ProductOptionResponse> getProductOptions() {
        List<ProductOptionResponse> productOptions = this.productRepository.findAll()
                .stream()
                .map((p)->{ return new ProductOptionResponse(p.getId(), p.getName());})
                .toList();
        return productOptions;
    }
}
