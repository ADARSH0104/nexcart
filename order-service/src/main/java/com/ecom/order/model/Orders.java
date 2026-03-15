package com.ecom.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatusEnum status;

    @Column(nullable = false)
    private Instant createdOn;

    @Column
    private Instant confirmedOn;

    @Column
    private BigDecimal totalAmount ;

    @Column
    private Long totalQuantity;

    @Column
    private Instant expiryAt;


    @PrePersist
    public void onCreate(){
        this.createdOn = Instant.now();
    }
    protected Orders() {
    }

    public Orders(Instant confirmedOn, Instant createdOn, Instant expiryAt, OrderStatusEnum status, Long userId) {
        this.confirmedOn = confirmedOn;
        this.createdOn = createdOn;
        this.expiryAt = expiryAt;
        this.status = status;
        this.userId = userId;
    }

    public static Orders create(Long userId) {
        return new Orders(null,null,null,OrderStatusEnum.CREATED,userId);
    }

    public UUID getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Instant getConfirmedOn() {
        return confirmedOn;
    }

    public void setConfirmedOn(Instant confirmedOn) {
        this.confirmedOn = confirmedOn;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }

    public void setExpiryAt(Instant expiryAt) {
        this.expiryAt = expiryAt;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void recalculateTotals(List<OrderItem> orderItems) {
        BigDecimal newPrice = BigDecimal.ZERO;
        Long newQuantity = 0L;
        for(OrderItem orderItem : orderItems){
            newPrice = newPrice.add(orderItem.getTotalPrice());
            newQuantity += orderItem.getTotalQuantity();
        }
        this.totalAmount = newPrice;
        this.totalQuantity = newQuantity;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }
}
