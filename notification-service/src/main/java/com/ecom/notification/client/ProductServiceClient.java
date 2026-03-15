package com.ecom.notification.client;

import com.ecom.notification.dto.OrderProductsSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE",
            path = "/api/v1/products")
public interface ProductServiceClient {

    @GetMapping("/details")
    OrderProductsSnapshot getDetails(@RequestParam List<Long> inventoryIds);
}
