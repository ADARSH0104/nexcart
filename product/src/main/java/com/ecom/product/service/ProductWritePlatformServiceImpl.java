package com.ecom.product.service;

import com.ecom.product.dto.ProductRequestDto;
import com.ecom.product.model.Category;
import com.ecom.product.model.Product;
import com.ecom.product.model.ProductImage;
import com.ecom.product.repository.CategoryRepository;
import com.ecom.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductWritePlatformServiceImpl implements ProductWritePlatformService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    @Value("${app.upload.dir}")
    private String uploadDir;
    public ProductWritePlatformServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public void addProduct(ProductRequestDto request,List<MultipartFile> productImages){
       try {
           Product product = new Product(request.name(), request.description());

           List<Category> categories = this.categoryRepository.findAllById(request.categoryIds());

           for (Category category : categories) {
               product.addCategory(category);
           }
           this.productRepository.save(product);

           Path productDir = Paths.get(uploadDir, "products", product.getId().toString());
           Files.createDirectories(productDir);

           for (MultipartFile file : productImages) {
               String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

               Path targetPath = productDir.resolve(filename);
               file.transferTo(targetPath.toFile());

               ProductImage image = new ProductImage(product, targetPath.toString());
               product.addImage(image);
           }
           this.productRepository.save(product);
       }catch (IOException e){
           throw new RuntimeException("Error creating new product :"+e.toString());
       }
       }
}
