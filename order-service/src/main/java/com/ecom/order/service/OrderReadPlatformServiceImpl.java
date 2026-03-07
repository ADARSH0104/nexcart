package com.ecom.order.service;

import com.ecom.order.client.InventoryServiceClient;
import com.ecom.order.dto.InventoryDetailDTO;
import com.ecom.order.dto.OrderDetailDTO;
import com.ecom.order.dto.OrderResDTO;
import com.ecom.order.model.Orders;
import com.ecom.order.model.OrderItem;
import com.ecom.order.repository.OrderEventRepository;
import com.ecom.order.repository.OrderItemRepository;
import com.ecom.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
class OrderReadPlatformServiceImpl implements OrderReadPlatformService{
 private final OrderRepository orderRepository;
 private final OrderEventRepository orderEventRepository;
 private final OrderItemRepository orderItemRepository;
 private final InventoryServiceClient inventoryServiceClient;
    public OrderReadPlatformServiceImpl(final OrderRepository orderRepository,
                                        final OrderEventRepository orderEventRepository,
                                        final OrderItemRepository orderItemRepository,
                                        final InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @Override
    public OrderResDTO getOrderDetails(String id) {
        Orders order = this.orderRepository.findById(UUID.fromString(id)).orElseThrow(()->new RuntimeException("Order not fount or expired"));

        List<OrderItem> orderItems = this.orderItemRepository.findAllByOrders_Id(order.getId());
        List<OrderDetailDTO>  orderDetailDTOS = validateOrder(orderItems);
//        this.orderItemRepository.saveAll(orderItems);

//        order.recalculateTotals(orderItems);
//        this.orderRepository.save(order);
        OrderResDTO res = new OrderResDTO(order.getId().toString(),orderDetailDTOS);
        return res;
    }

    private List<OrderDetailDTO> validateOrder(List<OrderItem> orderItems) {
        List<OrderDetailDTO> orderDetailDTOS = new ArrayList<>();
        for(OrderItem orderItem:orderItems){
            Boolean isOutOfStock = false;
            Boolean isQuantityChanged = false;
            Boolean isPriceChanged = false;
            InventoryDetailDTO inventoryDetailDTO = this.inventoryServiceClient.getInventory(orderItem.getInventoryId());
            if(inventoryDetailDTO.quantity()==0) {
                isOutOfStock = true;
            }

            if(orderItem.getUnitPrice()!=inventoryDetailDTO.price()){
                isPriceChanged = true;
//                orderItem.setUnitPrice(inventoryDetailDTO.price());
            }

            if(orderItem.getTotalQuantity()>inventoryDetailDTO.quantity()){
                isQuantityChanged = true;
//                orderItem.setTotalQuantity(inventoryDetailDTO.quantity());
            }

            orderDetailDTOS.add(new OrderDetailDTO(orderItem.getInventoryId(),orderItem.getTotalQuantity(),orderItem.getTotalPrice(),orderItem.getUnitPrice(),isOutOfStock,isQuantityChanged,isPriceChanged));
        }
        return orderDetailDTOS;
    }

}
