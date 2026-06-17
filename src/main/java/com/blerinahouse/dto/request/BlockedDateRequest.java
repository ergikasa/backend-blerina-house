package com.blerinahouse.dto.request;

import com.blerinahouse.validation.ValidDateRange;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@ValidDateRange(start = "blockedFrom", end = "blockedTo")      // <-- cross-field
public record BlockedDateRequest(
        @NotNull Long roomId,
        @NotNull @FutureOrPresent LocalDate blockedFrom,
        @NotNull @Future LocalDate blockedTo,
        @Size(max = 255) String reason
) {}