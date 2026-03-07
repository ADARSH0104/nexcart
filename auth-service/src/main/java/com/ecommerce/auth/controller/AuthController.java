package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.SignUpRequest;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.SignupResponse;
import com.ecommerce.auth.service.AuthWritePlatformService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthWritePlatformService authWritePlatformService;
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest request, HttpServletResponse response){
        return authWritePlatformService.authenticateUser(request,response);
    }

    @PostMapping("/register")
    public ResponseEntity<SignupResponse> registerUser(@Valid @RequestBody SignUpRequest request){
        return authWritePlatformService.registerUser(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> rotateTokens(@CookieValue(value = "refreshToken") String refreshToken, HttpServletResponse response){
        return authWritePlatformService.rotateTokens(refreshToken,response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@CookieValue(value = "refreshToken") String refreshToken,HttpServletResponse response){
        return authWritePlatformService.logout(refreshToken,response);
    }

}
