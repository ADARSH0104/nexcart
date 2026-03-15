package com.ecom.payment.service;

import com.ecom.payment.dto.PaymentResultEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class PaymentKafkaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaEventProducer.class);
    private KafkaTemplate<UUID, PaymentResultEventDTO> kafkaTemplate;

    public PaymentKafkaEventProducer(KafkaTemplate<UUID, PaymentResultEventDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //Sync calls
    public void send(String topic,UUID key, PaymentResultEventDTO message){
        try {
            var result = kafkaTemplate.send(topic,key,message).get();
            log.info("Payment event confirmed -> partition {} ,offset{}",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Failed to send payment event: {}", e.getMessage());
            throw new RuntimeException("Payment event delivery failed", e);
        }
    }
}
