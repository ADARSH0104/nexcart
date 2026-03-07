package com.ecom.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record RazorpayOrderResponseDTO(
        int amount,
        @JsonProperty("amount_due") int amountDue,
        @JsonProperty("amount_paid") int amountPaid,
        int attempts,
        @JsonProperty("created_at") long createdAt,
        String currency,
        String entity,
        String id,
        Map<String, String> notes,
        @JsonProperty("offer_id") String offerId,
        String receipt,
        String status
) {}
