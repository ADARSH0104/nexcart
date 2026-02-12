package com.ecom.product.service;

import com.ecom.product.dto.CreateInventoryRequestDTO;
import com.ecom.product.dto.InventoryManageDTO;
import com.ecom.product.dto.InventoryRequestDTO;
import com.ecom.product.model.Inventory;

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
