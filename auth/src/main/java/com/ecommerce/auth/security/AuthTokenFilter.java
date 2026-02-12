package com.ecommerce.auth.security;

import com.ecommerce.auth.service.CustomUserDetailsService;
import com.ecommerce.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.rmi.ServerException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                         HttpServletResponse response,
                         FilterChain filterChain)
    throws ServletException, IOException {
        try {
            String token = parseJwt(request);
            if (token != null && jwtUtil.validateToken(token)) {
                String userName = this.jwtUtil.getUsernameFromToken(token);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (Exception e){
            System.out.println("Cannot set user authentication: " + e);
        }
        filterChain.doFilter(request,response);
    }
    public String parseJwt(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")){
         return header.substring(7);
        }
        return null;
    }
}
