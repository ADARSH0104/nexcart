package com.ecom.product.inventory.service;

import com.ecom.product.inventory.dto.CreateInventoryRequestDTO;
import com.ecom.product.inventory.dto.InventoryManageDTO;
import com.ecom.product.inventory.dto.InventoryRequestDTO;

public interface InventoryWritePlatformService {

    void createInventory(CreateInventoryRequestDTO requestDTO);

    void addStock(Long inventoryId, InventoryManageDTO requestDTO);

    void priceUpdate(Long inventoryId, InventoryManageDTO requestDTO);

    void adjustStock(Long inventoryId, InventoryManageDTO requestDTO);

    void reserve(Long inventoryId, InventoryRequestDTO requestDTO);

    void release(Long inventoryId, InventoryRequestDTO requestDTO);

    void confirm(Long inventoryId, InventoryRequestDTO requestDTO);

    void returnStock(Long inventoryId, InventoryRequestDTO requestDTO);



//    void manageInventory(ManageInventoryRequestDTO requestDTO);
//    void addInventoryLog(Long inventoryId ,String action);
}
