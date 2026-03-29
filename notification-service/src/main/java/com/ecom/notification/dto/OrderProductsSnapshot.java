package com.ecom.notification.dto;

import java.util.Map;

public record OrderProductsSnapshot(Map<Long,ProductSnapshot> products) {
}
