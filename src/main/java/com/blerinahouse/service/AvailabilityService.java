package com.blerinahouse.service;

import com.blerinahouse.dto.response.AvailabilityResponse;
import com.blerinahouse.entity.Room;
import com.blerinahouse.entity.enums.ReservationStatus;
import com.blerinahouse.exception.BusinessRuleException;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.repository.BlockedDateRepository;
import com.blerinahouse.repository.ReservationRepository;
import com.blerinahouse.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final BlockedDateRepository blockedDateRepository;

    @Transactional(readOnly = true)
    public AvailabilityResponse check(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: id=" + roomId));
        if (Boolean.FALSE.equals(room.getIsActive())) {
            throw new BusinessRuleException("Room is not active");
        }
        boolean available = isAvailable(roomId, checkIn, checkOut, null);
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal pricePerNight = resolvePricePerNight(room, checkIn, checkOut);
        BigDecimal total = pricePerNight.multiply(BigDecimal.valueOf(nights));
        return new AvailabilityResponse(roomId, checkIn, checkOut, nights, available, pricePerNight, total);
    }

    /** Ripërdoret nga booking algorithm (3.10). excludeReservationId për editim. */
    @Transactional(readOnly = true)
    public boolean isAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeReservationId) {
        boolean reservedOverlap = (excludeReservationId == null)
                ? reservationRepository.existsOverlap(roomId, checkIn, checkOut, ReservationStatus.BLOCKING)
                : reservationRepository.existsOverlapExcluding(
                roomId, excludeReservationId, checkIn, checkOut, ReservationStatus.BLOCKING);
        boolean blockedOverlap = blockedDateRepository.existsOverlap(roomId, checkIn, checkOut);
        return !reservedOverlap && !blockedOverlap;
    }

    /** Hook për dynamic pricing (sezonale/ulje). Tani: base price. */
    public BigDecimal resolvePricePerNight(Room room, LocalDate checkIn, LocalDate checkOut) {
        return room.getBasePrice();
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) throw new BusinessRuleException("Dates are required");
        if (!checkOut.isAfter(checkIn)) throw new BusinessRuleException("checkOut must be after checkIn");
        if (checkIn.isBefore(LocalDate.now())) throw new BusinessRuleException("checkIn cannot be in the past");
    }
}