package com.ecom.cart.service;

import com.ecom.cart.client.InventoryFeignClient;
import com.ecom.cart.dto.CartResponseDTO;
import com.ecom.cart.dto.InventoryDetailDTO;
import com.ecom.cart.dto.ItemDetailDTO;
import com.ecom.cart.model.Cart;
import com.ecom.cart.model.CartItem;
import com.ecom.cart.repository.CartItemRepository;
import com.ecom.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartReadPlatformServiceImpl implements  CartReadPlatformService{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryFeignClient inventoryFeignClient;

    public CartReadPlatformServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,final InventoryFeignClient inventoryFeignClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    @Transactional
    @Override
    public CartResponseDTO getCart(Long userId) {
        Cart cart = this.cartRepository.findByUserId(userId);

        if(cart == null){
            return new CartResponseDTO(null, List.of(), 0L, true);
        }

        List<CartItem> cartItems = this.cartItemRepository.findAllByCartId(cart.getId());
        List<ItemDetailDTO> items = new ArrayList<>();

        for(CartItem cartItem:cartItems){
            //TODO create get all with inventory ida in inventory service;
            ItemDetailDTO item= updateItemDetails(cartItem);
            items.add(item);
        }
        this.cartItemRepository.saveAll(cartItems);
        cart.recomputeTotals(cartItems);
        this.cartRepository.save(cart);

        CartResponseDTO responseDTO = new CartResponseDTO(cart.getId(),items,cart.getTotalQuantity(),items.size()==0);
        return  responseDTO;
    }
    @Transactional
    @Override
    public ItemDetailDTO getItem(Long inventoryId,Long userId) {
        CartItem cartItem = this.cartItemRepository.findByUserIdAndInventoryId(userId,inventoryId);

        if(cartItem == null )
            return null;
        ItemDetailDTO responseDTO =updateItemDetails(cartItem);

        this.cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.recomputeTotals(this.cartItemRepository.findAllByCartId(cart.getId()));
        this.cartRepository.save(cart);

        return  responseDTO;
    }

    public ItemDetailDTO updateItemDetails(CartItem cartItem){
        InventoryDetailDTO inventoryDetailDTO = this.inventoryFeignClient.getInventory(cartItem.getInventoryId());
        BigDecimal productPrice = inventoryDetailDTO.price();
        Boolean isOutOfStock = inventoryDetailDTO.quantity()==0;
        Boolean priceChanged = false;
        if(!productPrice.equals(cartItem.getUnitPrice())){
            priceChanged = true;
            cartItem.setUnitPrice(productPrice);
        }
        Boolean quantityChanged = false;
        if(cartItem.getQuantity()>inventoryDetailDTO.quantity()){
            quantityChanged = true;
            cartItem.setQuantity(inventoryDetailDTO.quantity());
        }
        return new ItemDetailDTO(cartItem.getId(),cartItem.getInventoryId(),cartItem.getQuantity(),cartItem.getUnitPrice(),isOutOfStock,priceChanged,quantityChanged);
    }
}
