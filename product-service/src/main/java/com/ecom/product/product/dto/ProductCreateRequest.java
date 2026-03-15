package com.ecom.product.product.dto;



import java.util.List;

public record ProductCreateRequest(String name, String description,String brand, List<Long> categoryIds) {
}
