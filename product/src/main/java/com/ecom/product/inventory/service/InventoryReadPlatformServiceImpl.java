package com.ecom.product.inventory.service;

import com.ecom.product.inventory.dto.InventoryDetailDTO;
import com.ecom.product.inventory.dto.ReservationResponseDTO;
import com.ecom.product.inventory.dto.ReservationStatus;
import com.ecom.product.inventory.model.Inventory;
import com.ecom.product.inventory.model.InventoryLog;
import com.ecom.product.inventory.model.InventoryStatusEnum;
import com.ecom.product.inventory.repository.InventoryLogRepository;
import com.ecom.product.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
class InventoryReadPlatformServiceImpl implements InventoryReadPlatformService {
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    public InventoryReadPlatformServiceImpl(final InventoryRepository inventoryRepository,
                                            final InventoryLogRepository inventoryLogRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryLogRepository = inventoryLogRepository;
    }


    @Override
    public InventoryDetailDTO getInventory(Long inventoryId) {
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(()->new RuntimeException("Inventory not found"));

        return new InventoryDetailDTO(inventory.getId(),inventory.getPrice(),inventory.getAvailableQuantity());
    }

    @Override
    public  List<ReservationResponseDTO> getReservationDetail(UUID orderId) {
            List<InventoryLog> latestLogs = this.inventoryLogRepository.fetchLatestLogs(orderId.toString());
            List<ReservationResponseDTO> res = new ArrayList<>();
            for(InventoryLog latestLog:latestLogs){
                if(latestLog.getEvent()== InventoryStatusEnum.RESERVED){
                    if(latestLog.getExpiryAt().isAfter(Instant.now())){
                        res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(),ReservationStatus.ACTIVE,latestLog.getExpiryAt()));
                    }else{
                        res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(), ReservationStatus.EXPIRED,null));
                     }
                }else if(latestLog.getEvent()== InventoryStatusEnum.CONFIRMED){
                    res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(),ReservationStatus.CONFIRMED,null));
                }else if(latestLog.getEvent()== InventoryStatusEnum.RELEASED){
                    res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(),ReservationStatus.RELEASED,null));
                }else{
                    res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(), ReservationStatus.UNKNOWN,null));
                }
            }
            return res;
        }
}
