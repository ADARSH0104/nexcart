package com.ecom.order.service;

import com.ecom.order.client.InventoryServiceClient;
import com.ecom.order.config.JsonUtil;
import com.ecom.order.dto.*;
import com.ecom.order.exception.InventoryReservationFailedException;
import com.ecom.order.exception.OrderNotFoundException;
import com.ecom.order.exception.OutOfStockException;
import com.ecom.order.model.OrderEvent;
import com.ecom.order.model.OrderItem;
import com.ecom.order.model.OrderStatusEnum;
import com.ecom.order.model.Orders;
import com.ecom.order.repository.OrderEventRepository;
import com.ecom.order.repository.OrderItemRepository;
import com.ecom.order.repository.OrderRepository;
import org.hibernate.query.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
class OrderWritePlatformServiceImpl implements OrderWritePlatformService{
    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderWritePlatformServiceImpl(OrderRepository orderRepository,
                                         OrderEventRepository orderEventRepository,
                                         OrderItemRepository orderItemRepository,
                                         InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @Transactional
    @Override
    public void initiate(OrderDetailReqDTO requestDTO) {
        InventoryDetailDTO response = this.inventoryServiceClient.getInventory(requestDTO.inventoryId());

        if(response.quantity()==0){
            throw new OutOfStockException("Item out of stock");
        }

        Boolean activeOrderExists  = this.orderItemRepository.findByInventoryIdAndOrders_UserIdAndOrders_expiryAtAfter(requestDTO.inventoryId(),requestDTO.userId(), Instant.now());
        if(activeOrderExists ) return;

        Orders order = Orders.create(requestDTO.userId());
        this.orderRepository.save(order);

        OrderItem orderItem = OrderItem.create(order,requestDTO.inventoryId(),response.price(),Math.min(requestDTO.quantity(),response.quantity()));
        this.orderItemRepository.save(orderItem);

        OrderEvent orderEvent = OrderEvent.created(order.getId(), JsonUtil.toJson(requestDTO));
        this.orderEventRepository.save(orderEvent);
    }

    @Override
    public void confirm(UUID orderId) {
        Orders order = this.orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found orderId:" + orderId));

        Boolean orderConfirmed = this.orderEventRepository.existsByOrderIdAndEvent_InventoryReserved(orderId);
        if (orderConfirmed) return;
        OrderEvent confirmEvent = OrderEvent.confirmationStarted(orderId, JsonUtil.toJson(orderId));
        this.orderEventRepository.save(confirmEvent);

        List<OrderItem> orderItems = this.orderItemRepository.findAllByOrders_Id(orderId);
        List<OrderItem> reservedItems = new ArrayList<>();
        try {
            for (OrderItem orderItem : orderItems) {
                InventoryStateResponse inventoryStateResponse = this.inventoryServiceClient.reserve(orderItem.getInventoryId(), new InventoryRequestDTO(orderId, orderItem.getTotalQuantity()));
                if (inventoryStateResponse.status() == InventoryStatus.NOT_AVAILABLE) {
                    OrderEvent reservationFailed = OrderEvent.inventoryReservationFailed(orderId, JsonUtil.toJson(new InventoryRequestDTO(orderId, orderItem.getTotalQuantity())));
                    this.orderEventRepository.save(reservationFailed);
                    throw new InventoryReservationFailedException("Inventory not available for inventoryId=" + orderItem.getInventoryId());
                }
                reservedItems.add(orderItem);
            }
            OrderEvent reservedEvent = OrderEvent.inventoryReserved(orderId, JsonUtil.toJson(orderId));
            this.orderEventRepository.save(reservedEvent);
            order.setStatus(OrderStatusEnum.CONFIRMED);
            this.orderRepository.save(order);
        } catch (InventoryReservationFailedException ex) {
            for (OrderItem item : reservedItems) {
                ResponseEntity res = this.inventoryServiceClient.release(item.getInventoryId(), new InventoryRequestDTO(orderId, item.getTotalQuantity()));
            }
            throw ex;
        }
    }


    @Override
    public void complete(UUID orderId) {
        kafka
    }
}
