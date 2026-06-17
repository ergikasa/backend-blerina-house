package com.blerinahouse.controller.public_;

import com.blerinahouse.dto.response.AvailabilityResponse;
import com.blerinahouse.service.AvailabilityService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor

public class AvailabilityController {

    private final AvailabilityService availabilityService;

    // GET /api/v1/availability?roomId=1&checkIn=2026-07-15&checkOut=2026-07-20
    @GetMapping
    public AvailabilityResponse check(
            @RequestParam @Positive Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        return availabilityService.check(roomId, checkIn, checkOut);
    }
}