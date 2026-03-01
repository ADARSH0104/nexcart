package com.ecom.notification.dto;

import java.util.List;

public record OrderProductsSnapshot(List<ProductSnapshot> products) {
}
