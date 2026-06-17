package com.blerinahouse.dto.response;

public record RoomImageResponse(
        Long id,
        String imageUrl,
        String altText,
        Integer displayOrder,
        Boolean isCover
) {}