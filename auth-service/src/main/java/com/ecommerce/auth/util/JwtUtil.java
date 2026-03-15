package com.ecommerce.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwsExpirationMs;

    private SecretKey key;

    private final static Logger log = LoggerFactory.getLogger(JwtUtil.class);
    @PostConstruct
    public void init(){
        this.key= Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(String email,String role,String userId){
        return Jwts.builder()
                .setSubject(email)
                .claim("role",role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()+jwsExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String getEmailFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String getRoleFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role",String.class);
    }

    public String getUserIdFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("userId",String.class);
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: ", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: ", e);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: ", e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: ", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: ", e);
        }
        return false;
    }
}
