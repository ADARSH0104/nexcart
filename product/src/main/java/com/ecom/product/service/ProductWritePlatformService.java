package com.ecom.product.service;

import com.ecom.product.dto.ProductRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductWritePlatformService {
    void addProduct(ProductRequestDto request, List<MultipartFile> productImages);
}
