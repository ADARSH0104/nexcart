package com.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @Email @NotBlank(message = "Email is required" ) @Size(max = 255, message = "Email length cannot be more than 255 characters") String email,
        @NotBlank(message = "Mobile no is required") @Size(min =10,max = 10,message = "10 digits mobile number is required") String mobileNo,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last Name is required") String lastName,
        @NotBlank(message = "Password is required") String password,
        @NotNull Boolean seller) {
}
