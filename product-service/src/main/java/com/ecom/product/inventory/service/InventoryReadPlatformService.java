package com.ecom.product.inventory.service;

import com.ecom.product.inventory.dto.InventoryDetailDTO;
import com.ecom.product.inventory.dto.SellerDetailDTO;
import com.ecom.product.inventory.dto.ReservationResponseDTO;
import com.ecom.product.inventory.dto.SellerDashboardResponse;
import com.ecom.product.inventory.dto.SellerInventoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface InventoryReadPlatformService {

    InventoryDetailDTO getInventory(Long inventoryId);

    List<ReservationResponseDTO> getReservationDetail(UUID orderId);

     List<SellerDetailDTO> getSellerDetails(Long productId);

    Page<SellerInventoryResponse> getSellerInventoy(Long sellerId,int page,int size);

    SellerDashboardResponse getSellerDashboard(Long sellerId);
}
