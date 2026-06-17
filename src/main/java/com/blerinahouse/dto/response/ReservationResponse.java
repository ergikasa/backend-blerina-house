package com.blerinahouse.dto.response;

import com.blerinahouse.entity.enums.PaymentStatus;
import com.blerinahouse.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ReservationResponse(
        Long id,
        String reservationCode,
        Long roomId,
        String roomName,
        GuestResponse guest,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer nights,
        Integer numberOfGuests,
        BigDecimal pricePerNight,
        BigDecimal totalPrice,
        ReservationStatus status,
        String specialRequests,
        PaymentStatus paymentStatus,
        OffsetDateTime createdAt
) {}