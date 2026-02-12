package com.ecom.order.dto;

import java.util.List;
import java.util.UUID;

public record OrderResDTO(String orderId, List<OrderDetailDTO> orderDetailDTOList) {
}
