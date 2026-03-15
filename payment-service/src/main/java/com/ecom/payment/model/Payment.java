package com.ecom.payment.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,unique = true)
    private UUID orderId;

    @Column
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentStatus status;

    @Column
    private Integer attempts;

    @Column
    private String razorpayOrderId;

    @Column
    private Instant createdAt;

    protected Payment() {
    }

    public Payment(BigDecimal amount, Integer attempts, Instant createdAt, UUID orderId, String razorpayOrderId, PaymentStatus status, Long userId) {
        this.amount = amount;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.orderId = orderId;
        this.razorpayOrderId = razorpayOrderId;
        this.status = status;
        this.userId = userId;
    }

    public static Payment create(BigDecimal amount, UUID orderId, Long userId){
        return new Payment(amount,0,null,orderId,null,PaymentStatus.PAYMENT_INITIATED,userId);
    }

    public UUID getId() {
        return id;
    }

    public void onCreate(){
        this.createdAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
