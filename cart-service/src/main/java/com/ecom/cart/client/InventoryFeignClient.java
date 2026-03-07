package com.ecom.cart.client;

import com.ecom.cart.dto.InventoryDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="PRODUCT-SERVICE",
        path = "/api/v1/inventory")
public interface InventoryFeignClient {

    @GetMapping("/{inventoryId}")
     InventoryDetailDTO getInventory(@PathVariable Long inventoryId);

}
