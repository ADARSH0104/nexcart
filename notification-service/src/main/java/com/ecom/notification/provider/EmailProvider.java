package com.ecom.notification.provider;

import com.ecom.notification.dto.EmailProductLine;

import java.math.BigDecimal;
import java.util.List;

public interface EmailProvider {

     void sendOrderConfirmedMail(String userMail, String username, BigDecimal amount, List<EmailProductLine> productLines);
}
