package com.blerinahouse.entity;

import com.blerinahouse.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gjenerohet nga service-i (sekuenca reservation_code_seq), JO nga JPA.
    @Column(name = "reservation_code", nullable = false, unique = true, length = 20)
    private String reservationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    // GENERATED ALWAYS AS (check_out - check_in) STORED -> read-only.
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "nights")
    private Integer nights;

    @Column(name = "number_of_guests", nullable = false)
    private Integer numberOfGuests;

    // Snapshot i çmimit në momentin e rezervimit.
    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    // M1: kolonë normale, e vendosur nga service-i (jo GENERATED).
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "special_requests", columnDefinition = "text")
    private String specialRequests;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}