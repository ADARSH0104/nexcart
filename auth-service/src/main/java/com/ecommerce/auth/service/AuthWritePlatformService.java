package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.SignUpRequest;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.SignupResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthWritePlatformService {

    ResponseEntity<SignupResponse> registerUser(SignUpRequest request);
    ResponseEntity<AuthResponse> authenticateUser(LoginRequest request, HttpServletResponse response);
    ResponseEntity<AuthResponse> rotateTokens(String refreshToken, HttpServletResponse response);
    ResponseEntity<Object> logout(String refreshToken,HttpServletResponse response);
}
