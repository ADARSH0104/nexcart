package com.ecommerce.auth.controller;

import com.ecommerce.auth.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HealthController {

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public String health() {
//        User user =
        return "UP";
    }
}
