package com.ecom.cart.client;

import com.ecom.cart.dto.InventoryDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="product-service",
        url = "http://localhost:8082/product-service",
        path = "/api/v1/inventory")
public interface InventoryFeignClient {

    @GetMapping("/{inventoryId}")
     InventoryDetailDTO getInventory(@PathVariable Long inventoryId);

}
