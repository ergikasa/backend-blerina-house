package com.blerinahouse.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 140) String slug,
        @NotBlank @Size(max = 50) String roomType,
        String description,
        @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal basePrice,
        @NotNull @Positive Integer capacity,
        @PositiveOrZero Integer sizeSqm,
        @Size(max = 120) String bedConfiguration,
        Boolean isActive
) {}