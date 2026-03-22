package com.ecom.order.service;

import com.ecom.order.dto.PaymentResultEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final OrderWritePlatformService orderWritePlatformService;

    public PaymentEventConsumer(OrderWritePlatformService orderWritePlatformService) {
        this.orderWritePlatformService = orderWritePlatformService;
    }

    @KafkaListener(topics = "payment-success", groupId = "order-group")
    public void handlePaymentSuccess(
            @Header(KafkaHeaders.RECEIVED_KEY) UUID key,
            @Payload PaymentResultEventDTO message) {
        log.info("Payment success event received - OrderId: {}, PaymentId: {}, Status: {}",
                key, message.paymentId(), message.status());

        this.orderWritePlatformService.paymentSuccess(key, message);
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-group")
    public void handlePaymentFailure(
            @Header(KafkaHeaders.RECEIVED_KEY) UUID key,
            @Payload PaymentResultEventDTO message) {

        log.info("Payment failure event received - OrderId: {}, PaymentId: {}, Status: {}",
                key, message.paymentId(), message.status());

        this.orderWritePlatformService.paymentFailed(key, message);
    }
}