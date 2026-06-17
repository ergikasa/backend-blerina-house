package com.blerinahouse.dto.request;

import com.blerinahouse.validation.ValidDateRange;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@ValidDateRange(start = "checkInDate", end = "checkOutDate")   // <-- cross-field
public record CreateReservationRequest(
        @NotNull(message = "Room is required") Long roomId,

        @NotBlank @Size(max = 100) String guestFirstName,
        @NotBlank @Size(max = 100) String guestLastName,
        @NotBlank @Email(message = "A valid email is required") @Size(max = 255) String guestEmail,
        @Size(max = 40) String guestPhone,
        @Size(max = 80) String guestCountry,

        @NotNull @FutureOrPresent LocalDate checkInDate,
        @NotNull @Future LocalDate checkOutDate,

        @NotNull @Positive(message = "At least one guest is required") Integer numberOfGuests,

        @Size(max = 2000) String specialRequests
) {}