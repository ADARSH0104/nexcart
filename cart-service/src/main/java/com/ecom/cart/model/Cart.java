package com.ecom.cart.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private Long userId;

    @Column
    private Long totalQuantity;

    @Column
    private BigDecimal totalCartValue;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedOn;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedOn = Instant.now();
    }

    protected Cart() {
    }

    private Cart(Long userId, Long totalQuantity, BigDecimal totalCartValue, Instant createdAt, Instant updatedOn) {
        this.userId = userId;
        this.totalQuantity = totalQuantity;
        this.totalCartValue = totalCartValue;
        this.createdAt = createdAt;
        this.updatedOn = updatedOn;
    }

    public static Cart create(Long userId){
        return new Cart(userId,0L,BigDecimal.ZERO,null,null);
    }
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalCartValue() {
        return totalCartValue;
    }

    public void setTotalCartValue(BigDecimal totalCartValue) {
        this.totalCartValue = totalCartValue;
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

  public void recomputeTotals(List<CartItem> items){
        BigDecimal newTotalCartValue = BigDecimal.ZERO;
        Long newTotalQuantity = 0L;
        for(CartItem item:items){
            newTotalQuantity += item.getQuantity();
            newTotalCartValue = newTotalCartValue.add(item.getTotalPrice());
        }
        this.totalQuantity = newTotalQuantity;
        this.totalCartValue = newTotalCartValue;
  }
}
