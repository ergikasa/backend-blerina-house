package com.blerinahouse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AvailabilityResponse(
        Long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int nights,
        boolean available,
        BigDecimal pricePerNight,
        BigDecimal estimatedTotal
) {}