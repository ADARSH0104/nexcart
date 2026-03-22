package com.ecom.notification.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private UUID orderId;

    @Column
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column
    private NotificationStatus status;

    @Column
    private Long noOfRetries;

    @Column
    private Instant createdAt;

    @Column
    private Instant sentAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    public void onCreate(){
        this.createdAt=Instant.now();
    }


    @PreUpdate
    public void onUpdate(){
        this.updatedAt = Instant.now();
    }
    protected Notification() {
    }
    public Notification(UUID orderId, Long userId, NotificationType type, NotificationStatus status, Instant sentAt, Instant updatedAt, Instant createdAt, Long noOfRetries) {
        this.orderId = orderId;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.sentAt = sentAt;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.noOfRetries = noOfRetries;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Long getNoOfRetries() {
        return noOfRetries;
    }

    public void setNoOfRetries(Long noOfRetries) {
        this.noOfRetries = noOfRetries;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
