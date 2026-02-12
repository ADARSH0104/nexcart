package com.ecom.cart.service;

import com.ecom.cart.dto.ItemRequestDTO;

public interface CartWritePlatformService {
    void saveItem(ItemRequestDTO requestDTO);

    void removeItem(Long itemId);

    void addQuantity(Long itemId);

    void decreaseQuantity(Long itemId);
}
