package com.ecom.notification.client;

import com.ecom.notification.dto.ProductDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "product-service",
            url = "http://localhost:8082/product-service",
            path = "/api/v1/products")
public interface ProductServiceClient {

    @GetMapping("/details")
    ProductDetailsDTO getDetails(List<Long> inventoryIds);
}
