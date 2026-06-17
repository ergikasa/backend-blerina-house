package com.blerinahouse.dto.response;

import java.math.BigDecimal;

public record PaymentIntentResponse(
        String clientSecret,
        String paymentIntentId,
        BigDecimal amount,
        String currency
) {}