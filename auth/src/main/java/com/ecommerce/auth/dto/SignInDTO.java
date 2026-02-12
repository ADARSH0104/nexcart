package com.ecommerce.auth.dto;

public record SignInDTO(String username, String email, String mobileNo, String firstName, String lastName, String password) {
}
