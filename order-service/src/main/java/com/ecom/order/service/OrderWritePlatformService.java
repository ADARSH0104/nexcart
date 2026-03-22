package com.ecom.order.service;

import com.ecom.order.dto.OrderDetailReqDTO;
import com.ecom.order.dto.OrderRequestDTO;
import com.ecom.order.dto.PaymentInitiateRespose;
import com.ecom.order.dto.PaymentResultEventDTO;

import java.util.UUID;

public interface OrderWritePlatformService {
    void  create(OrderDetailReqDTO requestDTO);

    void confirm(UUID orderId);

    PaymentInitiateRespose intitiatePayment(UUID orderId);

    void paymentFailed(UUID key, PaymentResultEventDTO message);

    void paymentSuccess(UUID key, PaymentResultEventDTO message);

    void deliver(UUID orderId);

    UUID createFromCart(Long userId);
}
