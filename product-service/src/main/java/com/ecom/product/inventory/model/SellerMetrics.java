package com.ecom.product.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "seller_metrics")
public class SellerMetrics {

    @Id
    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long totalProducts;

    @Column(nullable = false)
    private Long activeListings;

    @Column(nullable = false)
    private Long pendingOrders;

    @Column(nullable = false)
    private Long totalAvailableQuantity;

    @Column(nullable = false)
    private Long totalReservedQuantity;

    @Column(nullable = false)
    private Long totalSoldQuantity;

    @Column(nullable = false)
    private Long totalDeliveredQuantity;

    @Column(nullable = false)
    private Long totalReturnQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRevenue;

    protected SellerMetrics() {
    }

    public SellerMetrics(Long sellerId) {
        this.sellerId = sellerId;
        initializeDefaults();
    }

    @PrePersist
    public void onCreate() {
        initializeDefaults();
    }

    private void initializeDefaults() {
        if (this.totalProducts == null) this.totalProducts = 0L;
        if (this.activeListings == null) this.activeListings = 0L;
        if (this.pendingOrders == null) this.pendingOrders = 0L;
        if (this.totalAvailableQuantity == null) this.totalAvailableQuantity = 0L;
        if (this.totalReservedQuantity == null) this.totalReservedQuantity = 0L;
        if (this.totalSoldQuantity == null) this.totalSoldQuantity = 0L;
        if (this.totalDeliveredQuantity == null) this.totalDeliveredQuantity = 0L;
        if (this.totalReturnQuantity == null) this.totalReturnQuantity = 0L;
        if (this.totalRevenue == null) this.totalRevenue = BigDecimal.ZERO;
    }

    public void incrementTotalProducts() {
        this.totalProducts += 1;
    }

    public void applyAvailabilityTransition(long previousAvailableQuantity, long currentAvailableQuantity) {
        boolean wasActive = previousAvailableQuantity > 0;
        boolean isActive = currentAvailableQuantity > 0;
        if (wasActive == isActive) {
            return;
        }

        this.activeListings += isActive ? 1 : -1;
    }

    public void adjustAvailableQuantity(long delta) {
        this.totalAvailableQuantity += delta;
    }

    public void adjustReservedQuantity(long delta) {
        this.totalReservedQuantity += delta;
    }

    public void adjustSoldQuantity(long delta) {
        this.totalSoldQuantity += delta;
    }

    public void adjustDeliveredQuantity(long delta) {
        this.totalDeliveredQuantity += delta;
    }

    public void adjustReturnQuantity(long delta) {
        this.totalReturnQuantity += delta;
    }

    public void adjustPendingOrders(long delta) {
        this.pendingOrders += delta;
    }

    public void adjustRevenue(BigDecimal delta) {
        this.totalRevenue = this.totalRevenue.add(delta);
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public Long getActiveListings() {
        return activeListings;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public Long getTotalAvailableQuantity() {
        return totalAvailableQuantity;
    }

    public Long getTotalReservedQuantity() {
        return totalReservedQuantity;
    }

    public Long getTotalSoldQuantity() {
        return totalSoldQuantity;
    }

    public Long getTotalDeliveredQuantity() {
        return totalDeliveredQuantity;
    }

    public Long getTotalReturnQuantity() {
        return totalReturnQuantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public void setActiveListings(Long activeListings) {
        this.activeListings = activeListings;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public void setTotalAvailableQuantity(Long totalAvailableQuantity) {
        this.totalAvailableQuantity = totalAvailableQuantity;
    }

    public void setTotalReservedQuantity(Long totalReservedQuantity) {
        this.totalReservedQuantity = totalReservedQuantity;
    }

    public void setTotalSoldQuantity(Long totalSoldQuantity) {
        this.totalSoldQuantity = totalSoldQuantity;
    }

    public void setTotalDeliveredQuantity(Long totalDeliveredQuantity) {
        this.totalDeliveredQuantity = totalDeliveredQuantity;
    }

    public void setTotalReturnQuantity(Long totalReturnQuantity) {
        this.totalReturnQuantity = totalReturnQuantity;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
