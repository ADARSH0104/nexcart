package com.ecom.order.dto;

import java.util.List;

public record OrderResDTO(String orderId, List<OrderDetailDTO> orderDetailDTOList) {
}
