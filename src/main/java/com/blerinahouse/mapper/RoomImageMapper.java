package com.blerinahouse.mapper;

import com.blerinahouse.dto.response.RoomImageResponse;
import com.blerinahouse.entity.RoomImage;
import com.blerinahouse.mapper.config.MapStructConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface RoomImageMapper {

    RoomImageResponse toResponse(RoomImage image);

    List<RoomImageResponse> toResponseList(List<RoomImage> images);
}