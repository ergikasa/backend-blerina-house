package com.blerinahouse.dto.response;

import java.time.LocalDate;

public record BlockedDateResponse(
        Long id, Long roomId, LocalDate blockedFrom, LocalDate blockedTo, String reason
) {}