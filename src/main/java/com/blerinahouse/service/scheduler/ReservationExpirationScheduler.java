package com.blerinahouse.service.scheduler;

import com.blerinahouse.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationScheduler.class);
    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "PT1M")   // çdo minutë
    public void expireStaleReservations() {
        int expired = reservationService.expireStale();
        if (expired > 0) log.info("Expired {} stale PENDING reservations", expired);
    }
}