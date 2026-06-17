package com.blerinahouse.controller.admin;

import com.blerinahouse.dto.response.PagedResponse;
import com.blerinahouse.dto.response.ReservationResponse;
import com.blerinahouse.entity.enums.ReservationStatus;
import com.blerinahouse.service.PaymentService;
import com.blerinahouse.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Admin panel: VETËM View, Cancel, Refund.
 * Konfirmimi bëhet AUTOMATIKISHT nga webhook-u i Stripe (payment_intent.succeeded),
 * ndaj nuk ka më endpoint manual "confirm".
 */
@RestController
@RequestMapping("/api/v1/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    // ---- VIEW ----
    @GetMapping
    public PagedResponse<ReservationResponse> list(
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reservationService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return reservationService.getById(id);
    }

    // ---- CANCEL ----
    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    // ---- REFUND (opsional) ----
    @PostMapping("/{id}/refund")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refund(@PathVariable Long id) {
        paymentService.refund(id);
    }

    // SHËNIM: confirm/check-in/check-out u hoqën sipas vendimit tim te ri se jam gango.
    // Metodat te ReservationService mbeten — nëse më vonë do check-in/out në recepsion,
    // i rikthen këto dy endpoint-e pa prekur asgjë tjetër.
}