package com.blerinahouse.mapper;

import com.blerinahouse.dto.response.GuestResponse;
import com.blerinahouse.entity.Guest;
import com.blerinahouse.mapper.config.MapStructConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface GuestMapper {

    GuestResponse toResponse(Guest guest);
}