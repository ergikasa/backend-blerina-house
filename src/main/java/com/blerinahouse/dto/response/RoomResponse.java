package com.blerinahouse.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(
        Long id,
        String name,
        String slug,
        String roomType,
        String description,
        BigDecimal basePrice,
        Integer capacity,
        Integer sizeSqm,
        String bedConfiguration,
        Boolean isActive,
        List<RoomImageResponse> images
) {}