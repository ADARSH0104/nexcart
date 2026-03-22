package com.ecom.payment.controller;

import com.ecom.payment.dto.PaymentCreateOrderRequestDTO;
import com.ecom.payment.dto.PaymentCreateOrderResponseDTO;
import com.ecom.payment.service.PaymentReadPlatformService;
import com.ecom.payment.service.PaymentWritePlatformService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentReadPlatformService paymentReadPlatformService;
    private final PaymentWritePlatformService paymentWritePlatformService;

    public PaymentController(PaymentReadPlatformService paymentReadPlatformService, PaymentWritePlatformService paymentWritePlatformService) {
        this.paymentReadPlatformService = paymentReadPlatformService;
        this.paymentWritePlatformService = paymentWritePlatformService;
    }

    @PostMapping("/createOrder")
    public ResponseEntity<PaymentCreateOrderResponseDTO> createOrder(@Valid @RequestBody PaymentCreateOrderRequestDTO req){
        PaymentCreateOrderResponseDTO res = this.paymentWritePlatformService.createOrder(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/razorpay/webhook")
    public void webhook(@RequestHeader("X-Razorpay-Signature") String signature,@RequestHeader("X-Razorpay-Event-Id") String razorpayEventId, @RequestBody String req){
        this.paymentWritePlatformService.handleWebhook(signature,razorpayEventId,req);
    }
}
