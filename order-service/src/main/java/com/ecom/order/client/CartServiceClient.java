package com.ecom.order.client;

import com.ecom.order.dto.CartResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CART-SERVICE",path = "/api/v1/cart")
public interface CartServiceClient {

    @GetMapping(value = "/{userId}")
    CartResponseDTO getCart(@PathVariable Long userId);
}
