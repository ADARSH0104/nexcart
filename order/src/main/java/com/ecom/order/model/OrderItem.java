package com.ecom.order.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id","inventory_id"})
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name="order_id",nullable = false)
    private Orders orders;

    @Column(name="inventory_id",nullable = false)
    private Long inventoryId;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private Long totalQuantity;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    public void onCreate(){
        this.createdAt= Instant.now();
    }
    @PreUpdate
    public void onUpdate(){
        this.updatedAt= Instant.now();
        this.totalPrice = BigDecimal.valueOf(this.totalQuantity).multiply(this.unitPrice);
    }
    protected OrderItem() {
    }

    private OrderItem(Orders orders, Long inventoryId, BigDecimal unitPrice, BigDecimal totalPrice, Long totalQuantity, Instant createdAt, Instant updatedAt) {
        this.orders = orders;
        this.inventoryId = inventoryId;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public static OrderItem create(Orders order, Long inventoryId, BigDecimal unitPrice,Long totalQuantity){
        return new OrderItem(order,inventoryId,unitPrice,null,totalQuantity,null,null);
    }
    public Orders getOrder() {
        return orders;
    }

    public void setOrder(Orders order) {
        this.orders = order;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
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
}
