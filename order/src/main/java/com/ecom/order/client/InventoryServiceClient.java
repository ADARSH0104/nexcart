package com.ecom.order.client;

import com.ecom.order.dto.InventoryDetailDTO;
import com.ecom.order.dto.InventoryRequestDTO;
import com.ecom.order.dto.InventoryStateResponse;
import com.ecom.order.dto.ReservationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "product-service",
        url = "http://localhost:8082/product-service",
        path="/api/v1/inventory"
        )
public interface InventoryServiceClient {

    @GetMapping("/{id}")
    InventoryDetailDTO getInventory(@PathVariable Long id);

    @GetMapping("/{orderId}/checkReservations")
    ReservationResponseDTO getReservationDetails(@PathVariable UUID orderId);

    @PostMapping("/{id}/reserve")
    InventoryStateResponse reserve(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO);

        @PostMapping("/{id}/release")
    ResponseEntity release(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO);

        @PostMapping("/{id}/confirm")
    ResponseEntity confirm(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO);

        @PostMapping("/{id}/return")
    ResponseEntity returnStock(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO);

//        @PostMapping("/{id}/reserve")
//    ResponseEntity reserve(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO);
//
//        @PostMapping("/{id}/reserve")
//    ResponseEntity reserve(@PathVariable Long id, @RequestBody InventoryRequestDTO requestDTO);


}
