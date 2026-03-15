package com.ecom.order.service;

import com.ecom.order.dto.PaymentResultEventDTO;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.UUID;

public class PaymentEventConsumer {
    private final OrderWritePlatformService orderWritePlatformService;

    public PaymentEventConsumer(OrderWritePlatformService orderWritePlatformService) {
        this.orderWritePlatformService = orderWritePlatformService;
    }

    @KafkaListener(topics = "payment-success",groupId = "order-group")
    public void handlePaymentSuccess(UUID key, PaymentResultEventDTO message){
        this.orderWritePlatformService.paymentSuccess(key,message);
    }
    @KafkaListener(topics = "payment-failed",groupId = "order-group")
    public void handlePaymentFailure(UUID key, PaymentResultEventDTO message){
        this.orderWritePlatformService.paymentFailed(key,message);

    }
}
