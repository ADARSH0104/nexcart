package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.SignUpRequest;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.SignupResponse;
import com.ecommerce.auth.exception.UnauthorizedException;
import com.ecommerce.auth.exception.UserAlreadyExistsException;
import com.ecommerce.auth.model.RefreshToken;
import com.ecommerce.auth.model.Role;
import com.ecommerce.auth.model.RoleTypeEnum;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthWritePlatformServiceImpl implements AuthWritePlatformService{
    private PasswordEncoder encoder;
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private RefreshTokenService refreshTokenService;
    private RefreshTokenRepository refreshTokenRepository;
    private RoleRepository roleRepository;


    public AuthWritePlatformServiceImpl(PasswordEncoder encoder,
                                        AuthenticationManager authenticationManager,
                                        UserRepository userRepository,
                                        JwtUtil jwtUtil,
                                        RefreshTokenService refreshTokenService,
                                        RefreshTokenRepository refreshTokenRepository,
                                        RoleRepository roleRepository) {
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ResponseEntity<SignupResponse> registerUser(SignUpRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException("User already exist with email : "+request.email());
        }
        Role role;
        if(request.seller()){
            role =  this.roleRepository.findByRoleTypeEnum(RoleTypeEnum.ROLE_SELLER);
        }else{
            role = this.roleRepository.findByRoleTypeEnum(RoleTypeEnum.ROLE_USER);
        }
        User newUser = new User (
                request.email(),
                request.mobileNo(),
                request.firstName(),
                request.lastName(),
                encoder.encode(request.password()),
                role
        );
        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignupResponse("User Created Successfully"));
    }


    @Override
    public ResponseEntity<AuthResponse> authenticateUser(LoginRequest request, HttpServletResponse response){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        String accessToken = jwtUtil.generateJwtToken(user.getEmail(),user.getRole().getRoleTypeEnum().toString());
        String refreshToken = refreshTokenService.generateRefreshToken(user);
        setCookie(refreshToken,response);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthResponse(accessToken,user.getRole().getRoleTypeEnum().toString()));
    }



    @Transactional
    @Override
    public ResponseEntity<AuthResponse> rotateTokens(String refreshToken, HttpServletResponse response){
        RefreshToken oldToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if(refreshTokenService.isTokenExpired(oldToken) ){
            oldToken.setRevoked(true);
            oldToken.setRevokedDate(Instant.now());
            refreshTokenRepository.save(oldToken);
            throw new UnauthorizedException("Refresh token expired");
        }
        User user = oldToken.getUser();

        oldToken.setRevoked(true);
        oldToken.setRevokedDate(Instant.now());
        refreshTokenRepository.save(oldToken);

        String newRefreshToken = refreshTokenService.generateRefreshToken(user);
        String accessToken = jwtUtil.generateJwtToken(user.getEmail(),user.getRole().getRoleTypeEnum().toString());

        setCookie(newRefreshToken,response);
        return ResponseEntity.ok(new AuthResponse(accessToken,user.getRole().getRoleTypeEnum().toString()));
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

        oldToken.setRevoked(true);
        oldToken.setRevokedDate(Instant.now());
        refreshTokenRepository.save(oldToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken","")
                .secure(true)
                .sameSite("Strict")
                .httpOnly(true)
                .maxAge(0)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());
        return ResponseEntity.ok(Map.of("logout","true"));
    }
}
