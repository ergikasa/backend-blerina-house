package com.blerinahouse.dto.response;

public record GuestResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String country
) {}