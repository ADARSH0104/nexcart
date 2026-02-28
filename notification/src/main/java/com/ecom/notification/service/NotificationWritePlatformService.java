package com.ecom.notification.service;

import com.ecom.notification.dto.NotificationDTO;

import java.util.UUID;

public interface NotificationWritePlatformService {

    void sendConfirmedMail(UUID orderId, NotificationDTO message);
}
