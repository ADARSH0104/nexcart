package com.ecom.cart.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id","inventory_id"}),
        indexes = @Index(name="idx_cart_item_cart", columnList="cart_id")
)
public class  CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="inventory_id" ,nullable = false)
    private Long inventoryId;

    @Column(nullable = false)
    private Long quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedOn;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        calculate();
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedOn = Instant.now();
        calculate();
    }

    protected CartItem() {
    }

    public CartItem(Long inventoryId, Long quantity, Cart cart, BigDecimal unitPrice, BigDecimal totalPrice, Instant createdAt, Instant updatedOn) {
        this.inventoryId = inventoryId;
        this.quantity = quantity;
        this.cart = cart;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.updatedOn = updatedOn;
    }

    public static CartItem create(Long inventoryId,Cart cart,BigDecimal unitPrice){
        return new CartItem(inventoryId,0L,cart,unitPrice,null,null,null);
    }
    public Long getId() {
        return id;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void calculate(){
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    public void decreaseQuantity() {
        this.quantity--;
    }

    public void increaseQuantity(Long quantity) {
        this.quantity+=quantity;
    }
}