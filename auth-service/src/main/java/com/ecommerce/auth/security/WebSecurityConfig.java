package com.ecommerce.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@EnableMethodSecurity
@Configuration
public class WebSecurityConfig {
//    private AuthEntryPointJwt unauthorizedHandler;
//    private JwtUtil jwtUtil;
//
//    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler, CustomUserDetailsService userDetailsService,JwtUtil jwtUtil) {
//        this.unauthorizedHandler = unauthorizedHandler;
//        this.jwtUtil = jwtUtil;
//    }

//    @Bean
//    public AuthTokenFilter authenticationJwtTokenFilter() {
//        return new AuthTokenFilter(jwtUtil);
//    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http){
//        try {
//            http.
//                    csrf(csrf -> csrf
//                            .ignoringRequestMatchers("/api/v1/auth/**"))
//                    .cors(cors -> cors.configurationSource(request -> {
//                        CorsConfiguration config = new CorsConfiguration();
//                        config.setAllowedOrigins(List.of("https://frontend.app"));
//                        config.setAllowCredentials(true);
//                        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
//                        config.setAllowedHeaders(List.of("*"));
//                        return config;
//                    }))
//                    .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
//                    .sessionManagement(sessionManagement->sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                    .authorizeHttpRequests(authorizeRequests->
//                            authorizeRequests
//                            .requestMatchers("/api/v1/auth/**").permitAll()
//                            .anyRequest().authenticated()
//                    );
//            http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//            return http.build();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
