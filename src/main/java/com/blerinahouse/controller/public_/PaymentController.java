package com.blerinahouse.controller.public_;

import com.blerinahouse.dto.request.CreatePaymentIntentRequest;
import com.blerinahouse.dto.response.PaymentIntentResponse;
import com.blerinahouse.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public PaymentIntentResponse createIntent(@Valid @RequestBody CreatePaymentIntentRequest req) {
        return paymentService.createPaymentIntent(req.reservationId());
    }

    // Stripe -> server. Trup i PAPËRPUNUAR (String) për verifikim nënshkrimi.
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }
}