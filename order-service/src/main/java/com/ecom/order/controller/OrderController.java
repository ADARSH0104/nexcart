package com.ecom.order.controller;

import com.ecom.order.dto.*;
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

   @PostMapping("/checkOut")
   public ResponseEntity<OrderCreateResponse> createFromCart(@RequestHeader(name="X-User-Id") Long userId){
        UUID  orderId = this.orderWritePlatformService.createFromCart(userId);
        this.orderWritePlatformService.confirm(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrderCreateResponse(orderId.toString()));

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
    public ResponseEntity<PaymentInitiateRespose> complete(@PathVariable UUID orderId){
        return ResponseEntity.ok(this.orderWritePlatformService.intitiatePayment(orderId));
    }

    @PostMapping("/deliver/{orderId}")
    public ResponseEntity deliver(@PathVariable UUID orderId){
        this.orderWritePlatformService.deliver(orderId);
        return ResponseEntity.ok().build();
    }
}
