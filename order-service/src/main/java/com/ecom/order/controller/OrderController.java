package com.ecom.order.controller;

import com.ecom.order.dto.OrderDetailReqDTO;
import com.ecom.order.dto.OrderRequestDTO;
import com.ecom.order.dto.OrderResDTO;
import com.ecom.order.service.OrderReadPlatformService;
import com.ecom.order.service.OrderWritePlatformService;
import jakarta.validation.Valid;
import org.hibernate.query.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
class OrderController {

    private final OrderReadPlatformService orderReadPlatformService;
    private final OrderWritePlatformService orderWritePlatformService;

    public OrderController(OrderReadPlatformService orderReadPlatformService, OrderWritePlatformService orderWritePlatformService) {
        this.orderReadPlatformService = orderReadPlatformService;
        this.orderWritePlatformService = orderWritePlatformService;
    }

   @GetMapping("/{id}/getOrderDetails")
   public ResponseEntity<OrderResDTO> getOrderDetails(@PathVariable String id){
        OrderResDTO res = this.orderReadPlatformService.getOrderDetails(id);
        return ResponseEntity.ok(res);
   }

    @PostMapping("/create")
    public ResponseEntity initiateOrder(@RequestBody @Valid OrderDetailReqDTO requestDTO){
        this.orderWritePlatformService.create(requestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity confirmOrder(@PathVariable UUID orderId){
        this.orderWritePlatformService.confirm(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payment/{orderId}")
    public ResponseEntity complete(@PathVariable UUID orderId){
        this.orderWritePlatformService.intitiatePayment(orderId);
        return ResponseEntity.ok().build();
    }
}
