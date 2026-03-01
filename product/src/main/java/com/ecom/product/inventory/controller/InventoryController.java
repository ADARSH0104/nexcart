package com.ecom.product.inventory.controller;

import com.ecom.product.inventory.dto.*;
import com.ecom.product.inventory.service.InventoryReadPlatformService;
import com.ecom.product.inventory.service.InventoryWritePlatformService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
//    @Autowired
    private InventoryWritePlatformService inventoryWritePlatformService;
    private InventoryReadPlatformService inventoryReadPlatformService;


    public InventoryController(InventoryWritePlatformService inventoryWritePlatformService, InventoryReadPlatformService inventoryReadPlatformService) {
        this.inventoryWritePlatformService = inventoryWritePlatformService;
        this.inventoryReadPlatformService = inventoryReadPlatformService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDetailDTO> getInventory(@PathVariable Long id){
        InventoryDetailDTO response=  this.inventoryReadPlatformService.getInventory(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/checkReservations")
    public ResponseEntity< List<ReservationResponseDTO>> getReservationDetails(@PathVariable UUID orderId){
        List<ReservationResponseDTO> response=  this.inventoryReadPlatformService.getReservationDetail(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createInventory")
    public ResponseEntity createInventory(@RequestBody CreateInventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.createInventory(requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/addStock")
    public ResponseEntity addStock(@PathVariable Long id, @RequestBody InventoryManageDTO requestDTO){
        this.inventoryWritePlatformService.addStock(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity reserve(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.reserve(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release")
    public ResponseEntity release(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.release(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity confirm(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.confirm(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/priceUpdate")
    public ResponseEntity priceUpdate(@PathVariable Long id, InventoryManageDTO requestDTO){
        this.inventoryWritePlatformService.priceUpdate(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/adjustStock")
    public ResponseEntity adjustStock(@PathVariable Long id, @RequestBody InventoryManageDTO requestDTO){
        this.inventoryWritePlatformService.adjustStock(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/return")
    public ResponseEntity returnStock(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.returnStock(id,requestDTO);
        return ResponseEntity.ok().build();
    }
}
