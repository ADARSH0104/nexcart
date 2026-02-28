package com.ecom.notification.service;

import com.ecom.notification.client.ProductServiceClient;
import com.ecom.notification.client.UserServiceClient;
import com.ecom.notification.dto.*;
import com.ecom.notification.model.Notification;
import com.ecom.notification.model.NotificationStatus;
import com.ecom.notification.model.NotificationType;
import com.ecom.notification.provider.EmailProvider;
import com.ecom.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationWritePlatformServiceImpl implements NotificationWritePlatformService {
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final NotificationRepository notificationRepository;
    private final EmailProvider emailProvider;

    public NotificationWritePlatformServiceImpl(final UserServiceClient userServiceClient,
                                                final ProductServiceClient productServiceClient,
                                                final NotificationRepository notificationRepository,
                                                final EmailProvider emailProvider) {
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;
        this.notificationRepository = notificationRepository;
        this.emailProvider = emailProvider;
    }

    @Override
    public void sendConfirmedMail(UUID orderId, NotificationDTO message) {
        Notification notification = new Notification(orderId,message.userId(), NotificationType.ORDER_CONFIRMED, NotificationStatus.PENDING,null,null,null,null);
        this.notificationRepository.save(notification);

        List<OrderItemDetailsDTO> detailsDTOS = message.orderDetails();
        List<Long> inventoryIds = detailsDTOS
                .stream()
                .map(d -> d.inventoryId()).toList();

        ProductDetailsDTO productDetailsDTO = this.productServiceClient.getDetails(inventoryIds);

        Map<Long, OrderItemDetailsDTO> map = detailsDTOS
                .stream()
                .collect(Collectors.toMap(OrderItemDetailsDTO::inventoryId, d -> d));

        List<EmailProductLine> productLines = productDetailsDTO
                .products()
                .stream()
                .map(p -> {
                    OrderItemDetailsDTO item = map.get(p.inventoryId());
                    return new EmailProductLine(p.name(), item.price(), item.quantity());
                }).toList();


        UserDetailDTO user = this.userServiceClient.getDetails(message.userId());

        this.emailProvider.sendOrderConfirmedMail(user.emailAddress(), user.username(), message.totalAmount(), productLines);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        this.notificationRepository.save(notification);
    }
}
