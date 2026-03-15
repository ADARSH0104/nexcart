package com.ecom.product.inventory.model;

import com.ecom.product.inventory.exception.InsufficientStockException;
import com.ecom.product.product.model.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory", uniqueConstraints = {@UniqueConstraint(columnNames = {"seller_id", "product_id"})})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "available_qty", nullable = false)
    private Long availableQuantity;

    @Column(name = "reserved_qty", nullable = false)
    private Long reservedQuantity;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(nullable = false)
    private Instant createdOn;

    @Column(nullable = true)
    private Instant updatedOn;

    @Column
    private Long soldQuantity;

    @Column
    private Long deliveredQuantity;

    @Column
    private Long returnQuantity;
    @Version
    private Long version;

    protected Inventory() {
    }

    public Inventory(Long sellerId, Product product, BigDecimal price, Long availableQuantity, Long reservedQuantity) {
        this.sellerId = sellerId;
        this.product = product;
        this.price = price;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public void validatePositiveQuantity(Long quantity) {
        if (quantity == null || quantity <= 0)
            throw new InsufficientStockException("Quantity must postive and greater than 0");
    }

    public Long getAvailableQuantity() {
        return availableQuantity;
    }

    public boolean isOutOfStock() {
        return this.availableQuantity == 0;
    }

    public void setAvailableQuantity(Long availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void setReservedQuantity(Long reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setVersion(Long version) {
        this.version = version;
    }


    public Long getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(Long soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public Long getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Long returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public Long getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(Long deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public Long getReservedQuantity() {
        return reservedQuantity;
    }



    @PreUpdate
    public void onUpate() {
        this.updatedOn = Instant.now();
    }

    @PrePersist
    public void onCreate() {
        this.isActive = true;
        this.createdOn = Instant.now();
        if (this.availableQuantity == null) this.availableQuantity = 0L;
        if (this.reservedQuantity == null) this.reservedQuantity = 0L;
        if (this.soldQuantity == null) this.soldQuantity = 0L;
        if (this.deliveredQuantity == null) this.deliveredQuantity = 0L;
        if (this.returnQuantity == null) this.returnQuantity = 0L;
    }

    public void addStock(Long quantity) {
        validatePositiveQuantity(quantity);
        this.availableQuantity += quantity;
    }

    public void reserve(Long quantity) {
        validatePositiveQuantity(quantity);
        if (this.availableQuantity >= quantity) {
            this.availableQuantity -= quantity;
            this.reservedQuantity += quantity;
        } else {
            throw new InsufficientStockException("Cannot order more than available quantity,Available Quantiy:" + this.availableQuantity);
        }
    }

    public void release(Long quantity) {
        validatePositiveQuantity(quantity);
        if (this.reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            this.availableQuantity += quantity;
        } else {
            throw new InsufficientStockException("Cannot release more than ordered quantity");
        }
    }

    public void confirm(Long quantity) {
        validatePositiveQuantity(quantity);
        if (this.reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            this.soldQuantity +=quantity;
        } else {
            throw new InsufficientStockException("Cannot confirm more than reserved quantity");
        }
    }

    public void deliver(Long quantity) {
        validatePositiveQuantity(quantity);
        long pendingDeliveryQuantity = this.soldQuantity - this.deliveredQuantity;
        if (pendingDeliveryQuantity >= quantity) {
            this.deliveredQuantity += quantity;
        } else {
            throw new InsufficientStockException("Cannot deliver more than confirmed quantity pending delivery");
        }
    }

    public void returnItems(Long quantity) {
        validatePositiveQuantity(quantity);
        this.availableQuantity += quantity;
        this.soldQuantity -=quantity;
        this.returnQuantity +=quantity;
    }

    public void adjust(Long quantity) {
        if (quantity < 0 && Math.abs(quantity) > this.availableQuantity) {
            throw new InsufficientStockException("Product quantity cannot be more then current available quantity");
        }
        this.availableQuantity += quantity;
    }

}
