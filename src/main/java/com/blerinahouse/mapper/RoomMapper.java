package com.blerinahouse.mapper;

import com.blerinahouse.dto.request.RoomRequest;
import com.blerinahouse.dto.response.RoomResponse;
import com.blerinahouse.entity.Room;
import com.blerinahouse.mapper.config.MapStructConfig;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

// uses -> RoomMapper di të mapojë edhe listën e imazheve
@Mapper(config = MapStructConfig.class, uses = RoomImageMapper.class)
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Room toEntity(RoomRequest request);

    // Update i pjesshëm i një dhome ekzistuese.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "slug", ignore = true)        // slug nuk ndryshohet pas krijimit
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RoomRequest request, @MappingTarget Room room);
}