package com.blerinahouse.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors      // null kur s'ka gabime fushe
) {
    public record FieldError(String field, String message) {}
}