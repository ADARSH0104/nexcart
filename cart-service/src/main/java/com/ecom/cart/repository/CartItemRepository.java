package com.ecom.cart.repository;

import com.ecom.cart.model.Cart;
import com.ecom.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findAllByCart(Cart cart);

    List<CartItem> findAllByCartId(Long cartId);

    CartItem findByCartIdAndInventoryId(Long cartId, Long inventoryId);

    boolean existsByCartIdAndInventoryId(Long cartId, Long inventoryId);

    CartItem findByInventoryId(Long inventoryId);


    CartItem findByCart_UserIdAndInventoryId(Long userId, Long inventoryId);
}
