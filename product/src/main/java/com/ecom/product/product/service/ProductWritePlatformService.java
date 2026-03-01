package com.ecom.product.product.service;

import com.ecom.product.product.dto.ProductCreateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductWritePlatformService {
    void addProduct(ProductCreateRequest request, List<MultipartFile> productImages);
}
