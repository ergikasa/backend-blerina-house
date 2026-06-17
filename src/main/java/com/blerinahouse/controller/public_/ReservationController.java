package com.blerinahouse.controller.public_;

import com.blerinahouse.dto.request.CreateReservationRequest;
import com.blerinahouse.dto.response.ReservationResponse;
import com.blerinahouse.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(req));
    }

    // Faqja e konfirmimit: turisti sheh rezervimin me kodin BH-2026-000123
    @GetMapping("/{code}")
    public ReservationResponse getByCode(@PathVariable String code) {
        return reservationService.getByCode(code);
    }
}