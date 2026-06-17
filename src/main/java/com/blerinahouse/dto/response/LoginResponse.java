package com.blerinahouse.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,     // "Bearer"
        long expiresIn,       // sekonda
        String username,
        String role
) {}