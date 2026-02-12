package com.ecom.product.dto;



import java.util.List;

public record ProductRequestDto(String name, String description, List<Long> categoryIds) {
}
