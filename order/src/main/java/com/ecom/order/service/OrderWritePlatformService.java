package com.ecom.order.service;

import com.ecom.order.dto.OrderDetailReqDTO;
import com.ecom.order.dto.OrderRequestDTO;

import java.util.UUID;

public interface OrderWritePlatformService {
    void  initiate(OrderDetailReqDTO requestDTO);

    void confirm(UUID orderId);

    void complete(UUID orderId);

}
