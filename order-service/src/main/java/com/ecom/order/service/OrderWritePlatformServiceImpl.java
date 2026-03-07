package com.ecom.order.service;

import com.ecom.order.client.InventoryServiceClient;
import com.ecom.order.client.PaymentServiceClient;
import com.ecom.order.config.JsonUtil;
import com.ecom.order.dto.*;
import com.ecom.order.exception.InvalidOrderStateException;
import com.ecom.order.exception.InventoryReservationFailedException;
import com.ecom.order.exception.OrderNotFoundException;
import com.ecom.order.exception.OutOfStockException;
import com.ecom.order.model.*;
import com.ecom.order.repository.OrderEventRepository;
import com.ecom.order.repository.OrderItemRepository;
import com.ecom.order.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
class OrderWritePlatformServiceImpl implements OrderWritePlatformService {
    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final NotificationEventProducer notificationEventProducer;

    public OrderWritePlatformServiceImpl(final OrderRepository orderRepository,
                                         final OrderEventRepository orderEventRepository,
                                         final OrderItemRepository orderItemRepository,
                                         final InventoryServiceClient inventoryServiceClient,
                                         final PaymentServiceClient paymentServiceClient,
                                         final NotificationEventProducer notificationEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.notificationEventProducer = notificationEventProducer;
    }

    @Transactional
    @Override
    public void create(OrderDetailReqDTO requestDTO) {
        InventoryDetailDTO response = this.inventoryServiceClient.getInventory(requestDTO.inventoryId());

        if (response.quantity() == 0) {
            throw new OutOfStockException("Item out of stock");
        }

        Boolean activeOrderExists = this.orderItemRepository.existsByInventoryIdAndOrders_UserIdAndOrders_expiryAtAfter(requestDTO.inventoryId(), requestDTO.userId(), Instant.now());
        if (activeOrderExists) return;

        Orders order = Orders.create(requestDTO.userId());
        this.orderRepository.save(order);

        OrderItem orderItem = OrderItem.create(order, requestDTO.inventoryId(), response.price(), Math.min(requestDTO.quantity(), response.quantity()));
        this.orderItemRepository.save(orderItem);

        order.recalculateTotals(Arrays.asList(orderItem));
        this.orderRepository.save(order);
        OrderEvent orderEvent = OrderEvent.created(order.getId(), JsonUtil.toJson(requestDTO));
        this.orderEventRepository.save(orderEvent);
    }

    @Override
    public void confirm(UUID orderId) {
        Orders order = this.orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found orderId:" + orderId));

        Boolean orderConfirmed = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.INVENTORY_RESERVED);
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
            order.setStatus(OrderStatusEnum.INVENTORY_RESERVED);
            this.orderRepository.save(order);
        } catch (InventoryReservationFailedException ex) {
            for (OrderItem item : reservedItems) {
                ResponseEntity res = this.inventoryServiceClient.release(item.getInventoryId(), new InventoryRequestDTO(orderId, item.getTotalQuantity()));
            }
            throw ex;
        }
    }


    @Transactional
    @Override
    public void intitiatePayment(UUID orderId) {
        Orders order = this.orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order with Id : " + orderId + " not found"));

        Boolean paymentInitiated = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.PAYMENT_INITIATED);
        if (paymentInitiated) throw new InvalidOrderStateException("Payment is already initiated ");

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.PAYMENT_SUCCEEDED);
        if (paymentSuccessful) throw new InvalidOrderStateException("Payment is already completed");

        PaymentCreateOrderRequestDTO reqBody = new PaymentCreateOrderRequestDTO(order.getId(), order.getUserId(), order.getTotalAmount());
        PaymentCreateOrderResponseDTO res = this.paymentServiceClient.createOrder(reqBody);
        if(res==null) return;

        order.setStatus(OrderStatusEnum.PAYMENT_PENDING);
        this.orderRepository.save(order);

        this.orderEventRepository.save(OrderEvent.paymentInitiated(orderId,JsonUtil.toJson(res)));
    }

    @Transactional
    @Override
    public void paymentFailed(UUID key, PaymentResultEventDTO message) {
        Boolean paymentInitiated = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_INITIATED);
        if (!paymentInitiated) throw new InvalidOrderStateException("Cannot fail a payment which is not initiated");

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_SUCCEEDED);
        if (paymentSuccessful) throw new InvalidOrderStateException("Payment is already completed");

        this.orderEventRepository.save(OrderEvent.paymentFailed(key,JsonUtil.toJson(message)));

    }

    @Transactional
    @Override
    public void paymentSuccess(UUID key, PaymentResultEventDTO message) {
        Orders order = this.orderRepository.findById(key).orElseThrow(() -> new OrderNotFoundException("Order not found"));

        Boolean paymentInitiated = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_INITIATED);
        if (!paymentInitiated) throw new InvalidOrderStateException("Cannot complete a payment which is not initiated");

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_SUCCEEDED);
        if (paymentSuccessful) throw new InvalidOrderStateException("Payment is already completed");

        this.orderEventRepository.save(OrderEvent.paymentSucceeded(key, JsonUtil.toJson(message)));

        order.setStatus(OrderStatusEnum.PAID);
        this.orderRepository.save(order);

        List<OrderItem> items = this.orderItemRepository.findAllByOrders_Id(key);
        List<OrderItemDetailsDTO> orderDetails = new ArrayList<>();
        for(OrderItem item:items){
            orderDetails.add(new OrderItemDetailsDTO(item.getInventoryId(),item.getTotalPrice(), item.getTotalQuantity()));
        }

        this.notificationEventProducer.send("order-confirmed", order.getId(), new NotificationDTO(order.getId(), order.getUserId(),order.getTotalAmount(),orderDetails));

    }
}
