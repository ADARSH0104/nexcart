package com.ecommerce.auth.service;

import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.RefreshTokenHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository,RefreshTokenHasher refreshTokenHasher) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    public String generateRefreshToken(User user){
        String plainToken = UUID.randomUUID().toString();
//        String hashedToken = refreshTokenHasher.hash(plainToken);
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setCreatedDate(Instant.now());
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs) );
        newRefreshToken.setToken(plainToken);
        newRefreshToken.setRevoked(false);
        newRefreshToken.setUser(user);
        refreshTokenRepository.save(newRefreshToken);

        return plainToken;
    }

    public boolean isTokenExpired(RefreshToken refreshToken){
        return refreshToken.getExpiryDate().isBefore(Instant.now());
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken){
        return ResponseCookie.from("refreshToken" ,refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }
}
