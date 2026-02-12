package com.ecom.product.controller;

import com.ecom.product.dto.ProductDetailResponseDTO;
import com.ecom.product.dto.ProductRequestDto;
import com.ecom.product.model.Product;
import com.ecom.product.model.ProductImage;
import com.ecom.product.repository.ProductImageRepository;
import com.ecom.product.repository.ProductRepository;
import com.ecom.product.service.ProductWritePlatformService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/products")
@RestController
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductWritePlatformService productWritePlatformService;
    private final ProductImageRepository productImageRepository;
    public ProductController(final ProductRepository productRepository,
                             final ProductWritePlatformService productWritePlatformService,
                             final ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productWritePlatformService = productWritePlatformService;
        this.productImageRepository = productImageRepository;
    }

    //Fix get all products
    @GetMapping("/getProductList")
    public ResponseEntity<Object> retrieveProducts(){
        List<Product> allProduct  = this.productRepository.findAll();
        return ResponseEntity.ok(allProduct);
    }

        @PostMapping(value = "/addProduct" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity addProduct(@RequestPart(value = "data") ProductRequestDto request , @RequestPart(value = "productImages")List<MultipartFile> productImages){
            this.productWritePlatformService.addProduct(request,productImages);
            return ResponseEntity.ok().build();
        }

    @GetMapping(value="/getProduct/{productId}")
    public ResponseEntity<Object> getProduct(@PathVariable Long productId){
           Product product = this.productRepository.findById(productId).get();
//        Set<Category> categories = product.getProductCategories().stream().map(ProductCategory::getCategory)
//                .collect(Collectors.toSet());
           Set<Long> imageIds = product.getProductImageSet().stream()
                   .map(ProductImage::getId)
                   .collect(Collectors.toSet());
//           Set<Resource> images = new HashSet<>();
//           for (String path : paths) {
//               Path imagePath = Paths.get(path);
////               File image = Files.readAllBytes(imagePath).;
//               Resource resource =  new UrlResource(imagePath.toUri());
//               images.add(resource);
//           }
           ProductDetailResponseDTO response = new ProductDetailResponseDTO(product.getId(), product.getName(), product.getDescription(), product.getCategory(),imageIds);
           return ResponseEntity.ok(response);

   }

    /// Change local image storage to cloudb  storage
    @GetMapping(value="/getImage/{productId}/{imageId}")
    public ResponseEntity<Resource> getImage(@PathVariable Long productId ,@PathVariable Long imageId)
    throws IOException {
           ProductImage productImage = this.productImageRepository.findByIdAndProductId(imageId, productId)
                   .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
           Path path = Paths.get(productImage.getImageUrl());

           Resource resource = new UrlResource(path.toUri());

        String contentType = Files.probeContentType(path);

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
           return ResponseEntity.ok()
                   .contentType(MediaType.parseMediaType(contentType))
                   .body(resource);

    }
}
