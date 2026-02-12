package com.ecommerce.auth.api;

import com.ecommerce.auth.dto.LoginDTO;
import com.ecommerce.auth.dto.SignInDTO;
import com.ecommerce.auth.dto.TokenDTO;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.AuthWritePlatformService;
import com.ecommerce.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthWritePlatformService authWritePlatformService;
    @PostMapping("/signin")
    public ResponseEntity<TokenDTO> authenticateUser(@RequestBody LoginDTO request, HttpServletResponse response){
        return ResponseEntity.ok(authWritePlatformService.authenticateUser(request,response));
    }

    @PostMapping("signup")
    public String registerUser(@RequestBody SignInDTO request){
        return authWritePlatformService.registerUser(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> rotateTokens(@CookieValue(value = "refreshToken") String refreshToken, HttpServletResponse response){
        return authWritePlatformService.rotateTokens(refreshToken,response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@CookieValue(value = "refreshToken") String refreshToken,HttpServletResponse response){
        return authWritePlatformService.logout(refreshToken,response);
    }

}
