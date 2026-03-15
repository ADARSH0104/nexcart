package com.ecom.product.inventory.dto;

import java.math.BigDecimal;

public record SellerDashboardResponse(
        Long totalProducts,
        Long activeListings,
        BigDecimal totalRevenue,
        Long pendingOrders,
        Long totalAvailableQuantity,
        Long totalReservedQuantity,
        Long totalSoldQuantity,
        Long totalDeliveredQuantity,
        Long totalReturnQuantity
) {
}
