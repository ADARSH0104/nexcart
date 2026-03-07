package com.ecom.payment.dto;

import java.util.Map;

public record RazorpayOrderRequestDTO(int amount,
                                      String currency,
                                      String receipt,
                                      Map<String, String> notes) {
}
