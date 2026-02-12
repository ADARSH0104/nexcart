package com.ecom.cart.service;

import com.ecom.cart.dto.CartResponseDTO;
import com.ecom.cart.dto.ItemDetailDTO;

public interface CartReadPlatformService {
    CartResponseDTO getCart(Long userId);

    ItemDetailDTO getItem(Long inventoryId,Long userId);
}
