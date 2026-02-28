package com.ecom.notification.consumer;

import com.ecom.notification.dto.NotificationDTO;
import com.ecom.notification.service.NotificationWritePlatformService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderEventConsumer {
    private final NotificationWritePlatformService notificationWritePlatformService;

    public OrderEventConsumer(NotificationWritePlatformService notificationWritePlatformService) {
        this.notificationWritePlatformService = notificationWritePlatformService;
    }

    @KafkaListener(groupId = "notification-service",topics ="order-confirmed")
    public void notifyCustomer(@Header(KafkaHeaders.RECEIVED_KEY) UUID key, @Payload NotificationDTO message){
        this.notificationWritePlatformService.sendConfirmedMail(key,message);
    }
}
