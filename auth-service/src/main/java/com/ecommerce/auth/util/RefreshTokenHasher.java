package com.ecommerce.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawToken) {
        return encoder.encode(rawToken);
    }

    public boolean matches(String rawToken, String hashedToken) {
        return encoder.matches(rawToken, hashedToken);
    }
}
