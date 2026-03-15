package com.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @Email  @NotBlank(message = "Email required") String email,
        @NotBlank(message = "Password is required") String password) {
}
