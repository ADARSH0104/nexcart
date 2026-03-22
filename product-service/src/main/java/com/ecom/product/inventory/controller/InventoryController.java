package com.ecom.product.inventory.controller;

import com.ecom.product.inventory.dto.*;
import com.ecom.product.inventory.service.InventoryReadPlatformService;
import com.ecom.product.inventory.service.InventoryWritePlatformService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
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
        return ResponseEntity.ok(this.inventoryReadPlatformService.getInventory(id));
    }

    @GetMapping("/{orderId}/checkReservations")
    public ResponseEntity< List<ReservationResponseDTO>> getReservationDetails(@PathVariable UUID orderId){
        List<ReservationResponseDTO> response=  this.inventoryReadPlatformService.getReservationDetail(orderId);
        return ResponseEntity.ok(response);
    }


    //get sellers all inventory data
    @GetMapping(value= "/sellerInventory")
    public ResponseEntity<Page<SellerInventoryResponse>> getSellerInventory(@RequestHeader("X-User-Id") Long sellerId,@RequestParam(name = "page",defaultValue = "0") int page ,@RequestParam(name= "size",defaultValue = "10") int size){
        return ResponseEntity.ok(this.inventoryReadPlatformService.getSellerInventoy(sellerId,page,size));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SellerDashboardResponse> getSellerDashboard(@RequestHeader("X-User-Id") Long sellerId) {
        return ResponseEntity.ok(this.inventoryReadPlatformService.getSellerDashboard(sellerId));
    }

    @PostMapping("/createInventory")
    public ResponseEntity createInventory(@RequestBody CreateInventoryRequestDTO requestDTO,@RequestHeader("X-User-Id") Long sellerId){
        this.inventoryWritePlatformService.createInventory(requestDTO,sellerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/addStock")
    public ResponseEntity addStock(@PathVariable Long id, @RequestBody InventoryManageDTO requestDTO){
        this.inventoryWritePlatformService.addStock(id,requestDTO);
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

    /*
    Inter service communication apis


            */
    @PostMapping("/{id}/reserve")
    public ResponseEntity<InventoryStateResponse> reserve(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        return ResponseEntity.ok(this.inventoryWritePlatformService.reserve(id,requestDTO));
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

    @PostMapping("/{id}/deliver")
    public ResponseEntity deliver(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.deliver(id,requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/return")
    public ResponseEntity returnStock(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO){
        this.inventoryWritePlatformService.returnStock(id,requestDTO);
        return ResponseEntity.ok().build();
    }
}
