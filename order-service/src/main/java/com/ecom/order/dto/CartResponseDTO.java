package com.ecom.order.dto;

import java.util.List;

public record CartResponseDTO(Long cartId, List<ItemDetailDTO> itemDetails, Long totalQuantity,Boolean isCartEmpty) {
}