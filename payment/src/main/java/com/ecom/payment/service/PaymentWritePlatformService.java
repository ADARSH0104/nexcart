package com.ecom.payment.service;

import com.ecom.payment.dto.PaymentCreateOrderRequestDTO;
import com.ecom.payment.dto.PaymentCreateOrderResponseDTO;
import jakarta.validation.Valid;

public interface PaymentWritePlatformService {
    PaymentCreateOrderResponseDTO createOrder(@Valid PaymentCreateOrderRequestDTO req);

    void handleWebhook(String signature,String eventId,String req);
}
