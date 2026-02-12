package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginDTO;
import com.ecommerce.auth.dto.SignInDTO;
import com.ecommerce.auth.dto.TokenDTO;
import com.ecommerce.auth.exception.UnauthorizedException;
import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.time.Instant;
import java.util.Map;

@Service
public class AuthWritePlatformServiceImpl implements AuthWritePlatformService{
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /// Todo
    ///Implement roles
    /// and send a json with isLogin flag and Roles obj
    @Override
    public TokenDTO authenticateUser(LoginDTO request, HttpServletResponse response){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername());
         String accessToken = jwtUtil.generateJwtToken(user.getUsername());
         String refreshToken = refreshTokenService.generateRefreshToken(user.getId());
        setCookie(refreshToken,response);
        return new TokenDTO(accessToken);
    }


    ///todo
    /// add json response
    @Override
    public String registerUser(SignInDTO request){
        if(userRepository.existsByUsername(request.username())){
            return "User already Exists";
        }
        User newUser = new User (
                request.username(),
                request.email(),
                request.mobileNo(),
                request.firstName(),
                request.lastName(),
                encoder.encode(request.password())
        );
        userRepository.save(newUser);
        return "User registered Successfully";
    }
    @Transactional
    @Override
    public ResponseEntity<TokenDTO> rotateTokens(String refreshToken,HttpServletResponse response){
        RefreshToken oldToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if(refreshTokenService.isTokenExpired(oldToken) || oldToken.isRevoked()){
            oldToken.setRevoked(true);
            oldToken.setRevokedDate(Instant.now());
            refreshTokenRepository.save(oldToken);
            throw new UnauthorizedException("Refresh token expired");
        }
        User user = oldToken.getUser();

        oldToken.setRevoked(true);
        oldToken.setRevokedDate(Instant.now());
        refreshTokenRepository.save(oldToken);

        String newRefreshToken = refreshTokenService.generateRefreshToken(user.getId());
        String accessToken = jwtUtil.generateJwtToken(user.getUsername());

        setCookie(newRefreshToken,response);
        return ResponseEntity.ok(new TokenDTO(accessToken));
    }

    public void setCookie(String refreshToken, HttpServletResponse response){
        ResponseCookie refreshCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE,refreshCookie.toString());
    }

    @Override
    public ResponseEntity<Object> logout(String refreshToken,HttpServletResponse response){
        RefreshToken oldToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if(refreshTokenService.isTokenExpired(oldToken) || oldToken.isRevoked()){
            oldToken.setRevoked(true);
            oldToken.setRevokedDate(Instant.now());
            refreshTokenRepository.save(oldToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        oldToken.setRevoked(true);
        oldToken.setRevokedDate(Instant.now());
        ResponseCookie cookie = ResponseCookie.from("refreshToken","")
                .secure(true)
                .sameSite("Strict")
                .httpOnly(true)
                .maxAge(0)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());
        return ResponseEntity.ok(Map.of("Logout","true"));
    }
}
