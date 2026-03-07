package com.ecom.cart.controller;

import com.ecom.cart.dto.CartResponseDTO;
import com.ecom.cart.dto.ItemDetailDTO;
import com.ecom.cart.dto.ItemRequestDTO;
import com.ecom.cart.service.CartReadPlatformService;
import com.ecom.cart.service.CartWritePlatformService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/api/v1/cart")
public class CartController {

    private final CartReadPlatformService readPlatformService;
    private final CartWritePlatformService writePlatformService;

    public CartController(CartReadPlatformService readPlatformService, CartWritePlatformService writePlatformService) {
        this.readPlatformService = readPlatformService;
        this.writePlatformService = writePlatformService;
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long userId){
        CartResponseDTO cartDto=this.readPlatformService.getCart(userId);
        return ResponseEntity.ok(cartDto);
    }
    @GetMapping(value = "/{userId}/{inventoryId}")
    public ResponseEntity<ItemDetailDTO> getItemDetails(@PathVariable Long userId,@PathVariable Long inventoryId){
        ItemDetailDTO itemDto=this.readPlatformService.getItem(userId,inventoryId);
        return ResponseEntity.ok(itemDto);
    }
    @PostMapping(value = "/addItem")
    public ResponseEntity addItem(@RequestBody ItemRequestDTO requestDTO) {
        this.writePlatformService.saveItem(requestDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{itemId}/removeItem")
    public ResponseEntity removeItem(@PathVariable Long itemId) {
        this.writePlatformService.removeItem(itemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/manageCart/{itemId}/addQuantity")
    public ResponseEntity addQuantity(@PathVariable Long itemId) {
        this.writePlatformService.addQuantity(itemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/manageCart/{itemId}/decreaseQuantity")
    public ResponseEntity decreaseQuantity(@PathVariable Long itemId) {
        this.writePlatformService.decreaseQuantity(itemId);
        return ResponseEntity.ok().build();
    }
}


