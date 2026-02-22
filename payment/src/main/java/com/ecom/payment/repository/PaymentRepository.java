package com.ecom.payment.repository;

import com.ecom.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Boolean existsByOrderId(UUID orderId);

    Payment findByRazorpayOrderId(String razorpayOrderId);
}
