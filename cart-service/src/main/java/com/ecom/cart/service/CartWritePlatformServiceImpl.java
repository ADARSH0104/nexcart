package com.ecom.cart.service;

import com.ecom.cart.client.InventoryFeignClient;
import com.ecom.cart.dto.InventoryDetailDTO;
import com.ecom.cart.dto.ItemRequestDTO;
import com.ecom.cart.model.Cart;
import com.ecom.cart.model.CartItem;
import com.ecom.cart.repository.CartItemRepository;
import com.ecom.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


@Service
public class CartWritePlatformServiceImpl implements CartWritePlatformService{

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryFeignClient inventoryFeignClient;

    public CartWritePlatformServiceImpl(final CartRepository cartRepository, final CartItemRepository cartItemRepository, final InventoryFeignClient inventoryFeignClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    @Transactional
    @Override
    public void saveItem(ItemRequestDTO requestDTO) {
        InventoryDetailDTO inventoryDetailDTO = this.inventoryFeignClient.getInventory(requestDTO.inventoryId());
        BigDecimal productPrice=inventoryDetailDTO.price();
        Long requestedQuantity = Optional.ofNullable(requestDTO.quantity()).orElse(1L);
        if(inventoryDetailDTO.quantity()==0){
            throw new RuntimeException("Out of stock");
        }
        Long userId = requestDTO.userId();
        Cart  cart = this.cartRepository.findByUserId(userId);
        if(cart==null){
            cart = Cart.create(userId);
            this.cartRepository.save(cart);
        }
        CartItem cartItem = this.cartItemRepository.findByCartIdAndInventoryId(cart.getId(),requestDTO.inventoryId());
        if(cartItem == null) {
            cartItem=CartItem.create(requestDTO.inventoryId(),cart,productPrice);
        }
        if (cartItem.getQuantity() + requestedQuantity > inventoryDetailDTO.quantity()) {
            throw new RuntimeException("Out of Stock");
        } else {
            cartItem.increaseQuantity(requestedQuantity);
            cartItem.setUnitPrice(productPrice) ;
        }
        this.cartItemRepository.save(cartItem);

        cart.recomputeTotals(this.cartItemRepository.findAllByCartId(cart.getId()));
        this.cartRepository.save(cart);

    }


    @Transactional
    @Override
    public void removeItem(Long itemId) {
        CartItem cartItem = this.cartItemRepository.findById(itemId).orElseThrow(()->new RuntimeException("Item not found"));
        this.cartItemRepository.deleteById(itemId);

        Cart cart = cartItem.getCart();
        cart.recomputeTotals(this.cartItemRepository.findAllByCartId(cart.getId()));
        this.cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void addQuantity(Long itemId) {

        CartItem cartItem = this.cartItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

        InventoryDetailDTO inventoryDetailDTO = this.inventoryFeignClient.getInventory(cartItem.getInventoryId());
        BigDecimal productPrice = inventoryDetailDTO.price();

       if (cartItem.getQuantity() + 1 > inventoryDetailDTO.quantity()) {
            throw new RuntimeException("Out of Stock");
        } else {
            cartItem.increaseQuantity(1L);
       }

            cartItem.setUnitPrice(productPrice);
            this.cartItemRepository.save(cartItem);

            Cart cart = cartItem.getCart();
            cart.recomputeTotals(this.cartItemRepository.findAllByCartId(cart.getId()));
            this.cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void decreaseQuantity(Long itemId) {

        CartItem cartItem = this.cartItemRepository.findById(itemId).orElseThrow(()->new RuntimeException("Item not found"));
        InventoryDetailDTO inventoryDetailDTO = this.inventoryFeignClient.getInventory(cartItem.getInventoryId());
        BigDecimal productPrice = inventoryDetailDTO.price();

        if(cartItem.getQuantity()==1) {
            this.cartItemRepository.deleteById(cartItem.getId());
        }else{
            cartItem.decreaseQuantity();
            cartItem.setUnitPrice(productPrice);
            this.cartItemRepository.save(cartItem);
        }

        Cart cart = cartItem.getCart();
        cart.recomputeTotals(this.cartItemRepository.findAllByCartId(cart.getId()));
        this.cartRepository.save(cart);
    }
}
