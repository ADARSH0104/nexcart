//package com.ecommerce.auth.security;
//
//import com.ecommerce.auth.util.JwtUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//import java.util.List;
//
//@Component
//public class  AuthTokenFilter extends OncePerRequestFilter {
//    private JwtUtil jwtUtil;
//    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);
//
//    public AuthTokenFilter(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                         HttpServletResponse response,
//                         FilterChain filterChain)
//    throws ServletException, IOException {
//        try {
//            String token = parseJwt(request);
//            if (token != null && jwtUtil.validateToken(token)) {
//                String email = this.jwtUtil.getEmailFromToken(token);
//                String roles = this.jwtUtil.getRoleFromToken(token);
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                        email,
//                        null,
//                        List.of(new SimpleGrantedAuthority(roles))
//                );
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//        }catch (Exception e){
//            log.error("Cannot set user authentication: ",e);
//        }
//        filterChain.doFilter(request,response);
//    }
//    public String parseJwt(HttpServletRequest request){
//        String header = request.getHeader("Authorization");
//        if(header!=null && header.startsWith("Bearer ")){
//         return header.substring(7);
//        }
//        return null;
//    }
//}
