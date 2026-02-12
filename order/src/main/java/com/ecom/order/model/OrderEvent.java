package com.ecom.order.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_event")
//        ,
//        uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"order_id", "order_status"})
//}
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name="order_status")
    private OrderEventType event;

    @Column(columnDefinition = "JSON", nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = Instant.now();
    }

    protected OrderEvent() {
    }

    private OrderEvent(UUID orderId, OrderEventType event, String payload, Instant createdAt) {
        this.orderId = orderId;
        this.event = event;
        this.payload = payload;
        this.createdAt = createdAt;
    }





    public static OrderEvent created(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.ORDER_CREATED, payload, null);
    }

    public static OrderEvent confirmationStarted(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.CONFIRMATION_STARTED, payload, null);
    }

    public static OrderEvent inventoryReserved(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.INVENTORY_RESERVED, payload, null);
    }

    public static OrderEvent inventoryReservationFailed(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.INVENTORY_RESERVATION_FAILED, payload, null);
    }

    public static OrderEvent inventoryReleased(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.INVENTORY_RELEASED, payload, null);
    }

    public static OrderEvent paymentInitiated(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.PAYMENT_INITIATED, payload, null);
    }

    public static OrderEvent paymentSucceeded(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.PAYMENT_SUCCEEDED, payload, null);
    }

    public static OrderEvent paymentFailed(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.PAYMENT_FAILED, payload, null);
    }

//    public static OrderEvent confirmed(UUID orderId, String payload) {
//        return new OrderEvent(orderId, OrderEventType.ORDER_CONFIRMED, payload, null);
//    }

    public static OrderEvent cancelled(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.ORDER_CANCELLED, payload, null);
    }

    public static OrderEvent completed(UUID orderId, String payload) {
        return new OrderEvent(orderId, OrderEventType.ORDER_COMPLETED, payload, null);
    }

//    public static OrderEvent compensationStarted(UUID orderId, String payload) {
//        return new OrderEvent(orderId, OrderEventType.COMPENSATION_STARTED, payload, null);
//    }
//
//    public static OrderEvent compensationCompleted(UUID orderId, String payload) {
//        return new OrderEvent(orderId, OrderEventType.COMPENSATION_COMPLETED, payload, null);
//    }
    public Long getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderEventType getOrderStatus() {
        return event;
    }

    public void setOrderStatus(OrderEventType event) {
        this.event = event;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
