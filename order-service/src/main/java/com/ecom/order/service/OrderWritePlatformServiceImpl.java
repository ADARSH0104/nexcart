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
        log.info("Creating order for userId={} inventoryId={} quantity={}",
                requestDTO.userId(), requestDTO.inventoryId(), requestDTO.quantity());

        InventoryDetailDTO response = this.inventoryServiceClient.getInventory(requestDTO.inventoryId());
        log.debug("Inventory fetched for inventoryId={} availableQuantity={} price={}",
                requestDTO.inventoryId(), response.quantity(), response.price());

        if (response.quantity() == 0) {
            log.warn("Order creation failed: Item out of stock inventoryId={}", requestDTO.inventoryId());
            throw new OutOfStockException("Item out of stock");
        }

        Boolean activeOrderExists = this.orderItemRepository.existsByInventoryIdAndOrders_UserIdAndOrders_expiryAtAfter(requestDTO.inventoryId(), requestDTO.userId(), Instant.now());
        if (activeOrderExists) {
            log.info("Order creation skipped: Active order already exists for userId={} inventoryId={}",
                    requestDTO.userId(), requestDTO.inventoryId());
            return;
        }

        Orders order = Orders.create(requestDTO.userId());
        this.orderRepository.save(order);
        log.info("Order entity created orderId={} userId={}", order.getId(), requestDTO.userId());

        OrderItem orderItem = OrderItem.create(order, requestDTO.inventoryId(), response.price(), Math.min(requestDTO.quantity(), response.quantity()));
        this.orderItemRepository.save(orderItem);
        log.debug("OrderItem saved orderId={} inventoryId={} quantity={} price={}",
                order.getId(), requestDTO.inventoryId(), orderItem.getTotalQuantity(), orderItem.getTotalPrice());

        order.recalculateTotals(Arrays.asList(orderItem));
        this.orderRepository.save(order);
        log.info("Order totals calculated orderId={} totalAmount={} totalQuantity={}",
                order.getId(), order.getTotalAmount(), order.getTotalQuantity());

        OrderEvent orderEvent = OrderEvent.created(order.getId(), JsonUtil.toJson(requestDTO));
        this.orderEventRepository.save(orderEvent);
        log.debug("Order created event stored orderId={}", order.getId());

        log.info("Order creation completed successfully orderId={}", order.getId());
    }

    @Transactional
    @Override
    public UUID createFromCart(Long userId) {
        log.info("Starting checkout from cart for userId={}", userId);
        try {
            CartResponseDTO cartDetails  = this.cartServiceClient.getCart(userId);
            log.debug("Cart fetched for userId={} itemCount={}", userId, cartDetails.itemDetails().size());

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
                log.debug("OrderItem added inventoryId={} quantity={} unitPrice={}",
                        item.inventoryId(), item.quantity(), item.unitPrice());
            }

            this.orderItemRepository.saveAll(orderItems);
            log.debug("All OrderItems persisted orderId={} itemCount={}", order.getId(), orderItems.size());

            order.recalculateTotals(orderItems);
            this.orderRepository.save(order);
            log.info("Order total calculated orderId={} totalAmount={} totalItems={}",
                    order.getId(), order.getTotalAmount(), order.getTotalQuantity());

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
        log.info("Starting order confirmation for orderId={}", orderId);

        Orders order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found orderId:" + orderId));
        log.debug("Order fetched orderId={} status={} userId={}",
                orderId, order.getStatus(), order.getUserId());

        Boolean orderConfirmed = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.INVENTORY_RESERVED);
        if (orderConfirmed) {
            log.info("Order confirmation skipped: inventory already reserved for orderId={}", orderId);
            return;
        }

        OrderEvent confirmEvent = OrderEvent.confirmationStarted(orderId, JsonUtil.toJson(orderId));
        this.orderEventRepository.save(confirmEvent);
        log.debug("Confirmation started event stored orderId={}", orderId);

        List<OrderItem> orderItems = this.orderItemRepository.findAllByOrders_Id(orderId);
        log.info("Attempting to reserve inventory for orderId={} itemCount={}", orderId, orderItems.size());

        List<OrderItem> reservedItems = new ArrayList<>();
        try {
            for (OrderItem orderItem : orderItems) {
                log.debug("Reserving inventory inventoryId={} quantity={} orderId={}",
                        orderItem.getInventoryId(), orderItem.getTotalQuantity(), orderId);

                InventoryStateResponse inventoryStateResponse = this.inventoryServiceClient.reserve(
                        orderItem.getInventoryId(),
                        new InventoryRequestDTO(orderId, orderItem.getTotalQuantity()));

                log.debug("Inventory reservation response inventoryId={} status={}",
                        orderItem.getInventoryId(), inventoryStateResponse.status());

                if (inventoryStateResponse.status() == InventoryStatus.NOT_AVAILABLE ||
                        inventoryStateResponse.status() == InventoryStatus.RESERVATION_FAILED) {
                    log.error("Inventory reservation failed inventoryId={} status={} orderId={}",
                            orderItem.getInventoryId(), inventoryStateResponse.status(), orderId);

                    OrderEvent reservationFailed = OrderEvent.inventoryReservationFailed(
                            orderId,
                            JsonUtil.toJson(new InventoryRequestDTO(orderId, orderItem.getTotalQuantity())));
                    this.orderEventRepository.save(reservationFailed);

                    throw new InventoryReservationFailedException(
                            "Inventory not available for inventoryId=" + orderItem.getInventoryId());
                }
                reservedItems.add(orderItem);
                log.debug("Inventory successfully reserved inventoryId={} orderId={}",
                        orderItem.getInventoryId(), orderId);
            }

            OrderEvent reservedEvent = OrderEvent.inventoryReserved(orderId, JsonUtil.toJson(orderId));
            this.orderEventRepository.save(reservedEvent);
            log.debug("Inventory reserved event stored orderId={}", orderId);

            order.setStatus(OrderStatusEnum.INVENTORY_RESERVED);
            this.orderRepository.save(order);
            log.info("Order confirmation completed orderId={} status={} reservedItemCount={}",
                    orderId, order.getStatus(), reservedItems.size());

        } catch (InventoryReservationFailedException ex) {
            log.warn("Rolling back inventory reservations orderId={} reservedItemCount={}",
                    orderId, reservedItems.size());

            for (OrderItem item : reservedItems) {
                log.debug("Releasing inventory inventoryId={} quantity={} orderId={}",
                        item.getInventoryId(), item.getTotalQuantity(), orderId);

                ResponseEntity res = this.inventoryServiceClient.release(
                        item.getInventoryId(),
                        new InventoryRequestDTO(orderId, item.getTotalQuantity()));

                log.debug("Inventory released inventoryId={} responseStatus={}",
                        item.getInventoryId(), res.getStatusCode());
            }

            log.error("Order confirmation failed orderId={}", orderId, ex);
            throw ex;
        }
    }


    @Transactional
    @Override
    public PaymentInitiateRespose intitiatePayment(UUID orderId) {
        log.info("Initiating payment for orderId={}", orderId);

        Orders order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with Id : " + orderId + " not found"));
        log.debug("Order fetched orderId={} status={} totalAmount={}",
                orderId, order.getStatus(), order.getTotalAmount());

        Boolean paymentInitiated = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.PAYMENT_INITIATED);
        if (paymentInitiated) {
            log.warn("Payment initiation skipped: payment already initiated for orderId={}", orderId);
            throw new InvalidOrderStateException("Payment is already initiated ");
        }

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.PAYMENT_SUCCEEDED);
        if (paymentSuccessful) {
            log.warn("Payment initiation skipped: payment already completed for orderId={}", orderId);
            throw new InvalidOrderStateException("Payment is already completed");
        }

        PaymentCreateOrderRequestDTO reqBody = new PaymentCreateOrderRequestDTO(
                order.getId(), order.getUserId(), order.getTotalAmount());
        log.debug("Creating payment order orderId={} userId={} amount={}",
                order.getId(), order.getUserId(), order.getTotalAmount());

        PaymentCreateOrderResponseDTO res = this.paymentServiceClient.createOrder(reqBody);
        if(res==null) {
            log.error("Payment order creation returned null response orderId={}", orderId);
            throw new InvalidOrderStateException("Error initiating payment for order");

        }
        log.debug("Payment order created orderId={} paymentResponse={}", orderId, res);

        order.setStatus(OrderStatusEnum.PAYMENT_PENDING);
        this.orderRepository.save(order);
        log.debug("Order status updated orderId={} status={}", orderId, order.getStatus());

        this.orderEventRepository.save(OrderEvent.paymentInitiated(orderId,JsonUtil.toJson(res)));
        log.info("Payment initiated successfully orderId={}", orderId);

        return new PaymentInitiateRespose(res.razorpayOrderId(),res.amount(),"INR");
    }

    @Transactional
    @Override
    public void paymentFailed(UUID key, PaymentResultEventDTO message) {
        log.info("Processing payment failure for orderId={}", key);

        Boolean paymentInitiated = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_INITIATED);
        if (!paymentInitiated) {
            log.error("Payment failure rejected: payment not initiated for orderId={}", key);
            throw new InvalidOrderStateException("Cannot fail a payment which is not initiated");
        }

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_SUCCEEDED);
        if (paymentSuccessful) {
            log.warn("Payment failure rejected: payment already succeeded for orderId={}", key);
            throw new InvalidOrderStateException("Payment is already completed");
        }

        this.orderEventRepository.save(OrderEvent.paymentFailed(key,JsonUtil.toJson(message)));
        log.error("Payment failed orderId={} reason={}", key, message);
    }

    @Transactional
    @Override
    public void paymentSuccess(UUID key, PaymentResultEventDTO message) {
        log.info("Processing payment success for orderId={}", key);

        Orders order = this.orderRepository.findById(key)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        log.debug("Order fetched orderId={} status={}", key, order.getStatus());

        Boolean paymentInitiated = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_INITIATED);
        if (!paymentInitiated) {
            log.error("Payment success rejected: payment not initiated for orderId={}", key);
            throw new InvalidOrderStateException("Cannot complete a payment which is not initiated");
        }

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(key, OrderEventType.PAYMENT_SUCCEEDED);
        if (paymentSuccessful) {
            log.warn("Payment success skipped: payment already completed for orderId={}", key);
            throw new InvalidOrderStateException("Payment is already completed");
        }

        List<OrderItem> items = this.orderItemRepository.findAllByOrders_Id(key);
        log.info("Confirming inventory for orderId={} itemCount={}", key, items.size());

        for (OrderItem item : items) {
            log.debug("Confirming inventory inventoryId={} quantity={} orderId={}",
                    item.getInventoryId(), item.getTotalQuantity(), key);

            this.inventoryServiceClient.confirm(
                    item.getInventoryId(),
                    new InventoryRequestDTO(key, item.getTotalQuantity()));

            log.debug("Inventory confirmed inventoryId={} orderId={}", item.getInventoryId(), key);
        }

        this.orderEventRepository.save(OrderEvent.paymentSucceeded(key, JsonUtil.toJson(message)));
        log.debug("Payment succeeded event stored orderId={}", key);

        order.setStatus(OrderStatusEnum.PAID);
        this.orderRepository.save(order);
        log.info("Order status updated orderId={} status={}", key, order.getStatus());

        List<OrderItemDetailsDTO> orderDetails = new ArrayList<>();
        for(OrderItem item:items){
            orderDetails.add(new OrderItemDetailsDTO(
                    item.getInventoryId(),item.getUnitPrice() ,item.getTotalPrice(), item.getTotalQuantity()));
        }
        log.debug("Prepared order details for notification orderId={} itemCount={}", key, orderDetails.size());

        this.notificationEventProducer.send(
                "order-confirmed",
                order.getId(),
                new NotificationDTO(order.getId(), order.getUserId(), order.getTotalAmount(), orderDetails));

        log.info("Payment success completed and notification sent orderId={}", key);
    }

    @Transactional
    @Override
    public void deliver(UUID orderId) {
        log.info("Starting order delivery for orderId={}", orderId);

        Orders order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        log.debug("Order fetched orderId={} status={}", orderId, order.getStatus());

        Boolean paymentSuccessful = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.PAYMENT_SUCCEEDED);
        if (!paymentSuccessful) {
            log.error("Delivery rejected: order not paid orderId={}", orderId);
            throw new InvalidOrderStateException("Cannot deliver an unpaid order");
        }

        Boolean orderCompleted = this.orderEventRepository.existsByOrderIdAndEvent(orderId, OrderEventType.ORDER_COMPLETED);
        if (orderCompleted) {
            log.info("Delivery skipped: order already completed orderId={}", orderId);
            return;
        }

        List<OrderItem> items = this.orderItemRepository.findAllByOrders_Id(orderId);
        log.info("Processing delivery for orderId={} itemCount={}", orderId, items.size());

        for (OrderItem item : items) {
            log.debug("Delivering inventory inventoryId={} quantity={} orderId={}",
                    item.getInventoryId(), item.getTotalQuantity(), orderId);

            this.inventoryServiceClient.deliver(
                    item.getInventoryId(),
                    new InventoryRequestDTO(orderId, item.getTotalQuantity()));

            log.debug("Inventory delivered inventoryId={} orderId={}", item.getInventoryId(), orderId);
        }

        this.orderEventRepository.save(OrderEvent.completed(orderId, JsonUtil.toJson(orderId)));
        log.debug("Order completed event stored orderId={}", orderId);

        order.setStatus(OrderStatusEnum.COMPLETED);
        this.orderRepository.save(order);
        log.info("Order delivery completed orderId={} status={}", orderId, order.getStatus());
    }
}