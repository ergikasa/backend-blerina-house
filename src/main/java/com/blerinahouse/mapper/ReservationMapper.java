package com.blerinahouse.mapper;

import com.blerinahouse.dto.response.ReservationResponse;
import com.blerinahouse.entity.Reservation;
import com.blerinahouse.entity.enums.PaymentStatus;
import com.blerinahouse.mapper.config.MapStructConfig;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

// uses GuestMapper -> mapon guest-in e nested-uar
@Mapper(config = MapStructConfig.class, uses = GuestMapper.class)
public interface ReservationMapper {

    // paymentStatus vjen si parametër i dytë (Payment është i ndarë, 1:1 unidirectional)
    @Mapping(target = "roomId", source = "reservation.room.id")
    @Mapping(target = "roomName", source = "reservation.room.name")
    @Mapping(target = "guest", source = "reservation.guest")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    ReservationResponse toResponse(Reservation reservation, PaymentStatus paymentStatus);
}