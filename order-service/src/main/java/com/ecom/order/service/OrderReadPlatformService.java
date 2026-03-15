package com.ecom.order.service;

import com.ecom.order.dto.OrderResDTO;

public interface OrderReadPlatformService {
    OrderResDTO getOrderDetails(String id);

}
