package com.blerinahouse.dto.response;

import com.blerinahouse.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long reservationId,
        String stripePaymentIntentId,   // pi_... (jo sekret)
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String paymentMethod,
        OffsetDateTime paidAt
) {}