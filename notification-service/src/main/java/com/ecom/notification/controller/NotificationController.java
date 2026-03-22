package com.ecom.notification.controller;

import com.ecom.notification.dto.EmailProductLine;
import com.ecom.notification.provider.EmailProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final JavaMailSender javaMailSender;
    private final EmailProvider emailProvider;

    public NotificationController(final JavaMailSender javaMailSender,
                                  final EmailProvider emailProvider) {
        this.javaMailSender = javaMailSender;
        this.emailProvider = emailProvider;
    }
//
//    @PostMapping
//    public void sendMail(){
//        List<EmailProductLine> lines = List.of(
//                new EmailProductLine("Nike Air Max", BigDecimal.valueOf(2999), 2L),
//                new EmailProductLine("Sony Headphones", BigDecimal.valueOf(24999), 1L)
//        );
//        emailProvider.sendOrderConfirmedMail("zzz@gmail.com", "Adarsh Honnavar", BigDecimal.valueOf(10000L), lines);
//    }
}
