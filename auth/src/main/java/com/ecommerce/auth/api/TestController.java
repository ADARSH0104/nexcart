package com.ecommerce.auth.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired

    @GetMapping("/all")
    public String publicApi(){
        return "This is public api";
    }

    @GetMapping("/protectdRoute")
    public String checkAuth(){
        return "Protected api";
    }
}
