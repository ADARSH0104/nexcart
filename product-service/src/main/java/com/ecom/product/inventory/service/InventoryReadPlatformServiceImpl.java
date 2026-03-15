package com.ecom.product.inventory.service;

import com.ecom.product.inventory.dto.*;
import com.ecom.product.inventory.model.Inventory;
import com.ecom.product.inventory.model.InventoryLog;
import com.ecom.product.inventory.model.InventoryStatusEnum;
import com.ecom.product.inventory.model.SellerMetrics;
import com.ecom.product.inventory.repository.InventoryLogRepository;
import com.ecom.product.inventory.repository.InventoryRepository;
import com.ecom.product.inventory.repository.SellerMetricsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class InventoryReadPlatformServiceImpl implements InventoryReadPlatformService {
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final SellerMetricsRepository sellerMetricsRepository;

    public InventoryReadPlatformServiceImpl(final InventoryRepository inventoryRepository,
                                            final InventoryLogRepository inventoryLogRepository,
                                            final SellerMetricsRepository sellerMetricsRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.sellerMetricsRepository = sellerMetricsRepository;
    }


    @Override
    public InventoryDetailDTO getInventory(Long inventoryId) {
        Inventory inventory = this.inventoryRepository.findById(inventoryId)
                .orElseThrow(()->new RuntimeException("Inventory not found"));

        return new InventoryDetailDTO(inventory.getId(),inventory.getSellerId(),inventory.getPrice(),inventory.getAvailableQuantity(),inventory.getAvailableQuantity());
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
                }else if(latestLog.getEvent()== InventoryStatusEnum.DELIVERED){
                    res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(),ReservationStatus.DELIVERED,null));
                }else if(latestLog.getEvent()== InventoryStatusEnum.RELEASED){
                    res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(),ReservationStatus.RELEASED,null));
                }else{
                    res.add(new ReservationResponseDTO(latestLog.getInventory().getId(),latestLog.getOrderId(), ReservationStatus.UNKNOWN,null));
                }
            }
            return res;
        }
    //To show multiple sellers in product page used by product
    @Override
    public List<SellerDetailDTO> getSellerDetails(Long productId) {
        List<Inventory> inventory = this.inventoryRepository.findByProduct_Id(productId);
        List<SellerDetailDTO> res = inventory.stream().map((i)->new SellerDetailDTO(i.getId(),i.getSellerId(),i.getPrice(),i.getAvailableQuantity())).toList();
        return res;
    }

    @Override
    public Page<SellerInventoryResponse> getSellerInventoy(Long sellerId,int page,int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Inventory> inventoryPage = this.inventoryRepository.findBySellerIdOrderByCreatedOn(sellerId,pageable);


        return inventoryPage.map(i ->
                new SellerInventoryResponse(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getAvailableQuantity(),
                        i.getReservedQuantity(),
                        i.getSoldQuantity(),
                        i.getReturnQuantity(),
                        i.getPrice()
                )
        );
    }

    @Override
    public SellerDashboardResponse getSellerDashboard(Long sellerId) {
        SellerMetrics sellerMetrics = this.sellerMetricsRepository.findById(sellerId)
                .orElseGet(() -> backfillSellerMetrics(sellerId));

        return new SellerDashboardResponse(
                sellerMetrics.getTotalProducts(),
                sellerMetrics.getActiveListings(),
                sellerMetrics.getTotalRevenue(),
                sellerMetrics.getPendingOrders(),
                sellerMetrics.getTotalAvailableQuantity(),
                sellerMetrics.getTotalReservedQuantity(),
                sellerMetrics.getTotalSoldQuantity(),
                sellerMetrics.getTotalDeliveredQuantity(),
                sellerMetrics.getTotalReturnQuantity()
        );
    }

    private SellerMetrics backfillSellerMetrics(Long sellerId) {
        List<Inventory> inventories = this.inventoryRepository.findAllBySellerId(sellerId);
        SellerMetrics sellerMetrics = new SellerMetrics(sellerId);
        if (inventories.isEmpty()) {
            return this.sellerMetricsRepository.save(sellerMetrics);
        }

        for (Inventory inventory : inventories) {
            sellerMetrics.incrementTotalProducts();
            sellerMetrics.applyAvailabilityTransition(0L, nullSafeLong(inventory.getAvailableQuantity()));
            sellerMetrics.adjustAvailableQuantity(nullSafeLong(inventory.getAvailableQuantity()));
            sellerMetrics.adjustReservedQuantity(nullSafeLong(inventory.getReservedQuantity()));
            sellerMetrics.adjustSoldQuantity(nullSafeLong(inventory.getSoldQuantity()));
            sellerMetrics.adjustDeliveredQuantity(nullSafeLong(inventory.getDeliveredQuantity()));
            sellerMetrics.adjustReturnQuantity(nullSafeLong(inventory.getReturnQuantity()));
        }
        sellerMetrics.adjustPendingOrders(Math.max(0L, sellerMetrics.getTotalSoldQuantity() - sellerMetrics.getTotalDeliveredQuantity()));

        return this.sellerMetricsRepository.save(sellerMetrics);
    }

    private long nullSafeLong(Long value) {
        return Optional.ofNullable(value).orElse(0L);
    }

}
