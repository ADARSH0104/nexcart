package com.ecom.product.service;

import com.ecom.product.dto.InventoryDetailDTO;
import com.ecom.product.dto.InventoryRequestDTO;
import com.ecom.product.dto.ReservationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface InventoryReadPlatformService {

    InventoryDetailDTO getInventory(Long inventoryId);

    List<ReservationResponseDTO> getReservationDetail(UUID orderId);
}
