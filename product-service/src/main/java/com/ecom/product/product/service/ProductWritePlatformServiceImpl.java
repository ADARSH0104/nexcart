package com.ecom.product.product.service;

import com.ecom.product.product.dto.ProductCreateRequest;
import com.ecom.product.product.model.Category;
import com.ecom.product.product.model.Product;
import com.ecom.product.product.model.ProductImage;
import com.ecom.product.product.repository.CategoryRepository;
import com.ecom.product.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ProductWritePlatformServiceImpl implements ProductWritePlatformService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageUploadService imageUploadService;
//    @Value("${app.upload.dir}")
//    private String uploadDir;
    public ProductWritePlatformServiceImpl(final ProductRepository productRepository,
                                           final CategoryRepository categoryRepository,
                                           final ImageUploadService imageUploadService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.imageUploadService = imageUploadService;
    }

    @Transactional
    @Override
    public void addProduct(ProductCreateRequest request, List<MultipartFile> productImages){
       try {
           Product product = new Product(request.name(), request.description(),request.brand());

           List<Category> categories = this.categoryRepository.findAllById(request.categoryIds());

           for (Category category : categories) {
               product.addCategory(category);
           }
           this.productRepository.save(product);

           for (MultipartFile file : productImages) {
                Map response = this.imageUploadService.uploadImage(file,product.getId());
                String url = (String) response.get("secure_url");
               ProductImage image = new ProductImage(product, url);
               product.addImage(image);
           }
           this.productRepository.save(product);
       }catch (Exception e){
           throw new RuntimeException("Error creating new repository :"+e.toString());
       }
       }
}
