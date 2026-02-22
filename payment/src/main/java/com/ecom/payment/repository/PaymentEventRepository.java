package com.ecom.payment.repository;

import com.ecom.payment.model.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
    Boolean existsByRazorpayEventId(String eventId);
}
