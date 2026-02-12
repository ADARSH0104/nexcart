package com.ecommerce.auth.service;

import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public String generateRefreshToken(Long userId){
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setCreatedDate(Instant.now());
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs) );
        newRefreshToken.setToken(UUID.randomUUID().toString());
        newRefreshToken.setRevoked(false);
        newRefreshToken.setUser(userRepository.findById(userId).get());
        refreshTokenRepository.save(newRefreshToken);

        return newRefreshToken.getToken();
    }

    public boolean isTokenExpired(RefreshToken refreshToken){
        return refreshToken.getExpiryDate().isBefore(Instant.now());
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken){
        return ResponseCookie.from("refreshToken" ,refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();
    }
}
