package com.ecom.product.product.controller;

import com.ecom.product.product.dto.*;
import com.ecom.product.product.model.Product;
import com.ecom.product.product.repository.ProductImageRepository;
import com.ecom.product.product.repository.ProductRepository;
import com.ecom.product.product.service.ProductReadPlatformService;
import com.ecom.product.product.service.ProductWritePlatformService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/api/v1/products")
@RestController
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductWritePlatformService productWritePlatformService;
    private final ProductImageRepository productImageRepository;
    private final ProductReadPlatformService productReadPlatformService;
    public ProductController(final ProductRepository productRepository,
                             final ProductWritePlatformService productWritePlatformService,
                             final ProductImageRepository productImageRepository,
                             final ProductReadPlatformService productReadPlatformService) {
        this.productRepository = productRepository;
        this.productWritePlatformService = productWritePlatformService;
        this.productImageRepository = productImageRepository;
        this.productReadPlatformService = productReadPlatformService;
    }

//    @GetMapping("/catrgory")
//    public Page<ProductSearchResponse> getByCategory(@RequestParam(required = true) String category){
//        return this.productReadPlatformService.getByCategory(category);
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity createProduct(@RequestPart(value = "data") ProductCreateRequest request , @RequestPart(value = "productImages")List<MultipartFile> productImages){
        this.productWritePlatformService.addProduct(request,productImages);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(this.productReadPlatformService.getProductById(id));
   }

   @GetMapping(value = "/search")
    public Page<ProductSearchResponse> getSearchResult(@RequestParam(required = false) String keyword,
                                                                       @RequestParam(required = false) List<String> brands,
                                                                        @RequestParam(required = false) String category,
                                                                       @RequestParam(required = false) BigDecimal minPrice,
                                                                       @RequestParam(required = false) BigDecimal maxPrice,
                                                                       @RequestParam(required = false) Boolean inStock,
                                                                       @RequestParam(defaultValue = "relevance") String sort,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size
                                                                ){

       ProductSearchRequest req = new ProductSearchRequest(keyword,brands,category,minPrice,maxPrice,inStock,sort,page,size);
        return this.productReadPlatformService.getSearchResult(req);
   }


    @GetMapping("/details")
    public ResponseEntity<OrderProductsSnapshot> getOrderProducts(@RequestParam List<Long> inventoryIds){
        return ResponseEntity.ok(this.productReadPlatformService.getOrderProducts(inventoryIds));
    }


    // TODO Add update price and stock  And handle thumbnail url
}
