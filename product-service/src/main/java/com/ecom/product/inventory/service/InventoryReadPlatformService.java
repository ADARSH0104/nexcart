package com.ecom.product.inventory.service;

import com.ecom.product.inventory.dto.InventoryDetailDTO;
import com.ecom.product.inventory.dto.ReservationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface InventoryReadPlatformService {

    InventoryDetailDTO getInventory(Long inventoryId);

    List<ReservationResponseDTO> getReservationDetail(UUID orderId);
}
