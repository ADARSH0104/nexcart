package com.ecom.order.client;

import com.ecom.order.dto.PaymentCreateOrderRequestDTO;
import com.ecom.order.dto.PaymentCreateOrderResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service",
        url = "http://localhost:8085",
        path = "api/v1/payments")
public interface PaymentServiceClient {

    @PostMapping("/createOrder")
    PaymentCreateOrderResponseDTO createOrder(@Valid @RequestBody PaymentCreateOrderRequestDTO req);
}
