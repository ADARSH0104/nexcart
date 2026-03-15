package com.ecom.order.service;

import com.ecom.order.client.CartServiceClient;
import com.ecom.order.client.InventoryServiceClient;
import com.ecom.order.client.PaymentServiceClient;
import com.ecom.order.config.JsonUtil;
import com.ecom.order.dto.*;
import com.ecom.order.exception.*;
import com.ecom.order.model.*;
import com.ecom.order.repository.OrderEventRepository;
import com.ecom.order.repository.OrderItemRepository;
import com.ecom.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
class OrderWritePlatformServiceImpl implements OrderWritePlatformService {
    private static final Logger log = LoggerFactory.getLogger(OrderWritePlatformServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final NotificationEventProducer notificationEventProducer;
    private final CartServiceClient cartServiceClient;

    public OrderWritePlatformServiceImpl(final OrderRepository orderRepository,
                                         final OrderEventRepository orderEventRepository,
                                         final OrderItemRepository orderItemRepository,
                                         final InventoryServiceClient inventoryServiceClient,
                                         final PaymentServiceClient paymentServiceClient,
                                         final NotificationEventProducer notificationEventProducer,
                                         final CartServiceClient cartServiceClient) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.notificationEventProducer = notificationEventProducer;
        this.cartServiceClient = cartServiceClient;
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

    @Transactional
    @Override
    public UUID createFromCart(Long userId) {
        try {
            CartResponseDTO cartDetails  = this.cartServiceClient.getCart(userId);
            if(cartDetails.itemDetails().isEmpty()){
                log.warn("Checkout failed: cart empty for userId={}", userId);
                throw new CartEmptyException(userId);
            }
            Orders order = Orders.create(userId);
            orderRepository.save(order);

            log.info("Order created with id={} for userId={}", order.getId(), userId);
            List<OrderItem> orderItems = new ArrayList<>();
            for(ItemDetailDTO item:cartDetails.itemDetails()){
                if(item.isOutOfStock()) {
                    log.error("Item Out Of Stock inventoryId={} quantity={}",item.inventoryId(),item.quantity());
                    throw new OutOfStockException(
                            "Item out of stock inventoryId=" + item.inventoryId());
                }
                OrderItem orderItem = OrderItem.create(order,item.inventoryId(),item.unitPrice(), item.quantity());
                orderItems.add(orderItem);
            }
            this.orderItemRepository.saveAll(orderItems);
            order.recalculateTotals(orderItems);
            this.orderRepository.save(order);
            log.info("Order total calculated orderId={} totalItems={}",order.getId(),order.getTotalQuantity());
            String payload = JsonUtil.toJson(cartDetails);
            OrderEvent event = OrderEvent.created(order.getId(),payload);
            this.orderEventRepository.save(event);
            log.debug("Order created event stored orderId={}", order.getId());
            log.info("Checkout completed successfully orderId={}", order.getId());

            return order.getId();
        }  catch (Exception ex) {

            log.error(
                    "Checkout failed for userId={} error={}",
                    userId,
                    ex.getMessage(),
                    ex
            );

            throw new CheckoutFailedException(
                    "Failed to create order from cart for userId=" + userId,
                    ex
            );
        }
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
                if (inventoryStateResponse.status() == InventoryStatus.NOT_AVAILABLE || inventoryStateResponse.status() == InventoryStatus.RESERVATION_FAILED) {
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

        List<OrderItem> items = this.orderItemRepository.findAllByOrders_Id(key);
        for (OrderItem item : items) {
            this.inventoryServiceClient.confirm(item.getInventoryId(), new InventoryRequestDTO(key, item.getTotalQuantity()));
        }

        this.orderEventRepository.save(OrderEvent.paymentSucceeded(key, JsonUtil.toJson(message)));

        order.setStatus(OrderStatusEnum.PAID);
        this.orderRepository.save(order);

        List<OrderItemDetailsDTO> orderDetails = new ArrayList<>();
        for(OrderItem item:items){
            orderDetails.add(new OrderItemDetailsDTO(item.getInventoryId(),item.getTotalPrice(), item.getTotalQuantity()));
        }

        this.notificationEventProducer.send("order-confirmed", order.getId(), new NotificationDTO(order.getId(), order.getUserId(),order.getTotalAmount(),orderDetails));

    }

    @Transactional
    @Override
    public void deliver(UUID orderId) {
        Orders order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.PAYMENT_SUCCEEDED);
        if (!paymentSuccessful) {
            throw new InvalidOrderStateException("Cannot deliver an unpaid order");
        }

        Boolean orderCompleted = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.ORDER_COMPLETED);
        if (orderCompleted) {
            return;
        }

        List<OrderItem> items = this.orderItemRepository.findAllByOrders_Id(orderId);
        for (OrderItem item : items) {
            this.inventoryServiceClient.deliver(item.getInventoryId(), new InventoryRequestDTO(orderId, item.getTotalQuantity()));
        }

        this.orderEventRepository.save(OrderEvent.completed(orderId, JsonUtil.toJson(orderId)));
        order.setStatus(OrderStatusEnum.COMPLETED);
        this.orderRepository.save(order);
    }
}
