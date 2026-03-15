package com.ecom.order.service;

import com.ecom.order.dto.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationEventProducer {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventProducer.class);
    private KafkaTemplate<UUID, NotificationDTO> kafkaTemplate;

    public NotificationEventProducer(KafkaTemplate<UUID, NotificationDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, UUID key, NotificationDTO message){
            kafkaTemplate.send(topic,key,message);
            log.info("Order confirmed and notificaion initaited for order Id={}",key);
    }
}
