package com.ecom.product.product.dto;

import java.util.Map;

public record OrderProductsSnapshot(Map<Long,ProductSnapshot> products) {
}
