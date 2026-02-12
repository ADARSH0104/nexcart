package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginDTO;
import com.ecommerce.auth.dto.SignInDTO;
import com.ecommerce.auth.dto.TokenDTO;
import com.ecommerce.auth.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthWritePlatformService {

    String registerUser(SignInDTO request);
    TokenDTO authenticateUser(LoginDTO request, HttpServletResponse response);
    ResponseEntity<TokenDTO> rotateTokens(String refreshToken,HttpServletResponse response);
    ResponseEntity<Object> logout(String refreshToken,HttpServletResponse response);
}
