package com.ecom.product.inventory.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_log",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"order_id", "event","inventory_id"})
        }
)
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatusEnum event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "order_id")
    private UUID orderId;

    @Column
    private Long quantity;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant expiryAt;

    public InventoryLog() {
    }

    public InventoryLog(InventoryStatusEnum event, Inventory inventory, UUID orderId, Long quantity, Instant expiryAt) {
        this.event = event;
        this.inventory = inventory;
        this.orderId = orderId;
        this.quantity = quantity;
        this.expiryAt = expiryAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public InventoryStatusEnum getEvent() {
        return event;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }

    public Long getId() {
        return id;
    }

    /// FACTORY METHODS
    public static InventoryLog createInventory(Inventory inventory) {
        return new InventoryLog(InventoryStatusEnum.INVENTORY_CREATED, inventory, null, 0L, null);
    }

    public static InventoryLog addStock(Inventory inventory, Long quantity) {
        return new InventoryLog(InventoryStatusEnum.STOCK_ADDED, inventory, null, quantity, null);
    }

    public static InventoryLog reserve(Inventory inventory, UUID orderId, Long quantity, Instant expiryAt) {
        return new InventoryLog(InventoryStatusEnum.RESERVED, inventory, orderId, quantity, expiryAt);
    }

    public static InventoryLog release(Inventory inventory, UUID orderId, Long quantity) {
        return new InventoryLog(InventoryStatusEnum.RELEASED, inventory, orderId, quantity, null);
    }

    public static InventoryLog confirm(Inventory inventory, UUID orderId, Long quantity) {
        return new InventoryLog(InventoryStatusEnum.CONFIRMED, inventory, orderId, quantity, null);
    }

    public static InventoryLog returnItems(Inventory inventory, UUID orderId, Long quantity) {
        return new InventoryLog(InventoryStatusEnum.RETURNED, inventory, orderId, quantity, null);
    }

    public static InventoryLog expire(Inventory inventory, UUID orderId, Long quantity) {
        return new InventoryLog(InventoryStatusEnum.EXPIRED, inventory, orderId, quantity, null);
    }

    public static InventoryLog adjust(Inventory inventory, Long quantity) {
        return new InventoryLog(InventoryStatusEnum.ADJUSTED, inventory, null, quantity, null);
    }

    public static InventoryLog priceUpdate(Inventory inventory) {
        return new InventoryLog(InventoryStatusEnum.PRICE_UPDATED, inventory, null, null, null);

    }
}
