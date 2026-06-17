package com.blerinahouse.repository;

import com.blerinahouse.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReservationId(Long reservationId);

    // Përdoret nga Stripe webhook (3.11) për të gjetur pagesën.
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}