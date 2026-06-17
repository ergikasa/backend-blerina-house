package com.blerinahouse.service;

import com.blerinahouse.dto.response.PaymentIntentResponse;
import com.blerinahouse.entity.Payment;
import com.blerinahouse.entity.Reservation;
import com.blerinahouse.entity.enums.PaymentStatus;
import com.blerinahouse.entity.enums.ReservationStatus;
import com.blerinahouse.exception.BusinessRuleException;
import com.blerinahouse.exception.PaymentProcessingException;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.repository.PaymentRepository;
import com.blerinahouse.repository.ReservationRepository;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StripeClient stripeClient;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    // ============================================================
    // 1) KRIJIM PaymentIntent  (PA @Transactional -> thirrje e jashtme)
    // ============================================================
    public PaymentIntentResponse createPaymentIntent(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: id=" + reservationId));
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Payment can only be initiated for PENDING reservations");
        }

        // Idempotencë: nëse ekziston tashmë një PaymentIntent, ripërdore.
        Optional<Payment> existing = paymentRepository.findByReservationId(reservationId);
        if (existing.isPresent() && existing.get().getStripePaymentIntentId() != null) {
            try {
                PaymentIntent pi = stripeClient.v1().paymentIntents()
                        .retrieve(existing.get().getStripePaymentIntentId());
                return new PaymentIntentResponse(pi.getClientSecret(), pi.getId(),
                        existing.get().getAmount(), existing.get().getCurrency());
            } catch (StripeException e) {
                throw new PaymentProcessingException("Failed to retrieve payment intent: " + e.getMessage());
            }
        }

        // BigDecimal EUR -> long cent
        long amountMinor = reservation.getTotalPrice()
                .movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true).build())
                .putMetadata("reservationId", reservation.getId().toString())
                .putMetadata("reservationCode", reservation.getReservationCode())
                .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("pi-create-" + reservation.getReservationCode())
                .build();

        PaymentIntent pi;
        try {
            pi = stripeClient.v1().paymentIntents().create(params, options);
        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to create payment intent: " + e.getMessage());
        }

        Payment payment = existing.orElseGet(Payment::new);
        payment.setReservation(reservation);
        payment.setStripePaymentIntentId(pi.getId());
        payment.setAmount(reservation.getTotalPrice());
        payment.setCurrency("EUR");
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);   // transaksion i shkurtër i vetin

        return new PaymentIntentResponse(pi.getClientSecret(), pi.getId(),
                payment.getAmount(), payment.getCurrency());
    }

    // ============================================================
    // 2) WEBHOOK  (@Transactional -> vetëm shkrime DB)
    // ============================================================
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new BusinessRuleException("Invalid Stripe signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handleSucceeded(event);
            case "payment_intent.payment_failed" -> handleFailed(event);
            default -> { /* eventet e tjera injorohen */ }
        }
    }

    private void handleSucceeded(Event event) {
        PaymentIntent pi = extractPaymentIntent(event);
        if (pi == null) return;
        paymentRepository.findByStripePaymentIntentId(pi.getId()).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.COMPLETED) return;   // idempotent
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(OffsetDateTime.now());
            Reservation res = payment.getReservation();
            if (res.getStatus() == ReservationStatus.PENDING) {
                res.setStatus(ReservationStatus.CONFIRMED);
                res.setConfirmedAt(OffsetDateTime.now());
            }
            // dirty checking -> UPDATE për payment + reservation
        });
    }

    private void handleFailed(Event event) {
        PaymentIntent pi = extractPaymentIntent(event);
        if (pi == null) return;
        paymentRepository.findByStripePaymentIntentId(pi.getId()).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
            }
        });
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        return (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
    }

    // ============================================================
    // 3) REFUND  (PA @Transactional -> thirrje e jashtme)
    // ============================================================
    public void refund(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for reservation: " + reservationId));
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessRuleException("Only COMPLETED payments can be refunded");
        }
        try {
            stripeClient.v1().refunds().create(
                    RefundCreateParams.builder()
                            .setPaymentIntent(payment.getStripePaymentIntentId())
                            .build());
        } catch (StripeException e) {
            throw new PaymentProcessingException("Refund failed: " + e.getMessage());
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(OffsetDateTime.now());
        paymentRepository.save(payment);
    }
}