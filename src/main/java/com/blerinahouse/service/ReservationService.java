package com.blerinahouse.service;

import com.blerinahouse.dto.request.CreateReservationRequest;
import com.blerinahouse.dto.response.PagedResponse;
import com.blerinahouse.dto.response.ReservationResponse;
import com.blerinahouse.entity.Guest;
import com.blerinahouse.entity.Payment;
import com.blerinahouse.entity.Reservation;
import com.blerinahouse.entity.Room;
import com.blerinahouse.entity.enums.PaymentStatus;
import com.blerinahouse.entity.enums.ReservationStatus;
import com.blerinahouse.exception.BusinessRuleException;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.exception.RoomNotAvailableException;
import com.blerinahouse.mapper.ReservationMapper;
import com.blerinahouse.repository.GuestRepository;
import com.blerinahouse.repository.PaymentRepository;
import com.blerinahouse.repository.ReservationRepository;
import com.blerinahouse.repository.RoomRepository;
import com.blerinahouse.util.ReservationCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationMapper reservationMapper;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final AvailabilityService availabilityService;   // i njëjti paketë -> pa import

    @Value("${booking.hold-minutes:15}")
    private int holdMinutes;

    // ============================================================
    // CREATE — algoritmi atomik i booking-ut (3.10)
    // ============================================================
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest req) {

        // 1) Valido datat
        validateBookingDates(req.checkInDate(), req.checkOutDate());

        // 2) Dhoma ekzistuese & aktive
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: id=" + req.roomId()));
        if (Boolean.FALSE.equals(room.getIsActive())) {
            throw new BusinessRuleException("Room is not active");
        }

        // 3) Kapaciteti
        if (req.numberOfGuests() > room.getCapacity()) {
            throw new BusinessRuleException(
                    "numberOfGuests exceeds room capacity (" + room.getCapacity() + ")");
        }

        // 4) Pre-check disponueshmërie (UX) — DB constraint mbetet garancia
        if (!availabilityService.isAvailable(room.getId(), req.checkInDate(), req.checkOutDate(), null)) {
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        // 5) Find-or-create guest
        Guest guest = findOrCreateGuest(req);

        // 6) Snapshot i çmimit (hook për dynamic pricing)
        BigDecimal pricePerNight =
                availabilityService.resolvePricePerNight(room, req.checkInDate(), req.checkOutDate());
        long nights = ChronoUnit.DAYS.between(req.checkInDate(), req.checkOutDate());
        BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(nights));

        // 7) Ndërto rezervimin PENDING
        Reservation r = new Reservation();
        r.setReservationCode(ReservationCodeGenerator.generate(
                LocalDate.now().getYear(), reservationRepository.nextReservationCodeSeq()));
        r.setRoom(room);
        r.setGuest(guest);
        r.setCheckInDate(req.checkInDate());
        r.setCheckOutDate(req.checkOutDate());
        r.setNumberOfGuests(req.numberOfGuests());
        r.setPricePerNight(pricePerNight);
        r.setTotalPrice(totalPrice);
        r.setStatus(ReservationStatus.PENDING);
        r.setSpecialRequests(req.specialRequests());
        r.setExpiresAt(OffsetDateTime.now().plusMinutes(holdMinutes));

        // 8) SAVE -> exclusion constraint = roje finale ndaj race condition
        try {
            reservationRepository.saveAndFlush(r);   // flush -> INSERT tani; kap violation brenda metodës
        } catch (DataIntegrityViolationException ex) {
            throw new RoomNotAvailableException(
                    "Room was just booked by someone else. Please choose other dates.");
        }

        // PaymentIntent (Stripe) vjen te 3.11; këtu paymentStatus = null
        return toResponse(r);
    }

    // ============================================================
    // READ
    // ============================================================
    @Transactional(readOnly = true)
    public ReservationResponse getByCode(String code) {
        return toResponse(reservationRepository.findByReservationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + code)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> list(ReservationStatus status, Pageable pageable) {
        Page<Reservation> page = (status == null)
                ? reservationRepository.findAll(pageable)
                : reservationRepository.findByStatus(status, pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    // ============================================================
    // STATE MACHINE (2.4)
    // ============================================================
    @Transactional
    public ReservationResponse confirm(Long id) {
        Reservation r = getEntity(id);
        requireStatus(r, ReservationStatus.PENDING);
        r.setStatus(ReservationStatus.CONFIRMED);
        r.setConfirmedAt(OffsetDateTime.now());
        return toResponse(r);
    }

    @Transactional
    public ReservationResponse cancel(Long id) {
        Reservation r = getEntity(id);
        if (r.getStatus() != ReservationStatus.PENDING && r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only PENDING or CONFIRMED reservations can be cancelled");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        r.setCancelledAt(OffsetDateTime.now());
        return toResponse(r);
    }

    @Transactional
    public ReservationResponse checkIn(Long id) {
        Reservation r = getEntity(id);
        requireStatus(r, ReservationStatus.CONFIRMED);
        r.setStatus(ReservationStatus.CHECKED_IN);
        return toResponse(r);
    }

    @Transactional
    public ReservationResponse checkOut(Long id) {
        Reservation r = getEntity(id);
        requireStatus(r, ReservationStatus.CHECKED_IN);
        r.setStatus(ReservationStatus.CHECKED_OUT);
        return toResponse(r);
    }

    /** Skadon rezervimet PENDING të vjetruara (thirret nga scheduler). */
    @Transactional
    public int expireStale() {
        return reservationRepository.expirePendingReservations(OffsetDateTime.now());
    }

    // ============================================================
    // HELPERS
    // ============================================================
    @Transactional(readOnly = true)
    public Reservation getEntity(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: id=" + id));
    }
    @Transactional(readOnly = true)
    public ReservationResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    private Guest findOrCreateGuest(CreateReservationRequest req) {
        return guestRepository.findByEmailIgnoreCase(req.guestEmail()).stream()
                .filter(g -> g.getFirstName().equalsIgnoreCase(req.guestFirstName())
                        && g.getLastName().equalsIgnoreCase(req.guestLastName()))
                .findFirst()
                .orElseGet(() -> {
                    Guest g = new Guest();
                    g.setFirstName(req.guestFirstName());
                    g.setLastName(req.guestLastName());
                    g.setEmail(req.guestEmail());
                    g.setPhone(req.guestPhone());
                    g.setCountry(req.guestCountry());
                    return guestRepository.save(g);
                });
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) throw new BusinessRuleException("Dates are required");
        if (!checkOut.isAfter(checkIn)) throw new BusinessRuleException("checkOut must be after checkIn");
        if (checkIn.isBefore(LocalDate.now())) throw new BusinessRuleException("checkIn cannot be in the past");
    }

    private void requireStatus(Reservation r, ReservationStatus expected) {
        if (r.getStatus() != expected) {
            throw new BusinessRuleException(
                    "Invalid transition from " + r.getStatus() + "; expected " + expected);
        }
    }

    private ReservationResponse toResponse(Reservation r) {
        PaymentStatus ps = paymentRepository.findByReservationId(r.getId())
                .map(Payment::getStatus).orElse(null);
        return reservationMapper.toResponse(r, ps);
    }
}