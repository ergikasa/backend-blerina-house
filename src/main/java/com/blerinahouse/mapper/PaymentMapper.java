package com.blerinahouse.mapper;

import com.blerinahouse.dto.response.PaymentResponse;
import com.blerinahouse.entity.Payment;
import com.blerinahouse.mapper.config.MapStructConfig;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface PaymentMapper {

    @Mapping(target = "reservationId", source = "reservation.id")
    PaymentResponse toResponse(Payment payment);
}