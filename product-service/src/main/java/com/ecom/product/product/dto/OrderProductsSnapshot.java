package com.ecom.product.product.dto;

import java.util.List;
public record OrderProductsSnapshot(List<ProductSnapshot> products) {
}
