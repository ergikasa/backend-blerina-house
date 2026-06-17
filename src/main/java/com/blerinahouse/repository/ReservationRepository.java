package com.blerinahouse.repository;

import com.blerinahouse.entity.Reservation;
import com.blerinahouse.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    // ---- OVERLAP (pre-check për UX) ----
    @Query("""
           select (count(r) > 0) from Reservation r
           where r.room.id = :roomId
             and r.status in :statuses
             and r.checkInDate < :checkOut
             and r.checkOutDate > :checkIn
           """)
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut,
                          @Param("statuses") Collection<ReservationStatus> statuses);

    // Varianti për editim: përjashto vetë rezervimin që po ndryshohet.
    @Query("""
           select (count(r) > 0) from Reservation r
           where r.room.id = :roomId
             and r.id <> :excludeId
             and r.status in :statuses
             and r.checkInDate < :checkOut
             and r.checkOutDate > :checkIn
           """)
    boolean existsOverlapExcluding(@Param("roomId") Long roomId,
                                   @Param("excludeId") Long excludeId,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut,
                                   @Param("statuses") Collection<ReservationStatus> statuses);

    // Rezervimet aktive brenda një intervali (p.sh. kalendari i admin-it).
    @Query("""
           select r from Reservation r
           where r.room.id = :roomId
             and r.status in :statuses
             and r.checkInDate < :rangeEnd
             and r.checkOutDate > :rangeStart
           order by r.checkInDate asc
           """)
    List<Reservation> findActiveInRange(@Param("roomId") Long roomId,
                                        @Param("rangeStart") LocalDate rangeStart,
                                        @Param("rangeEnd") LocalDate rangeEnd,
                                        @Param("statuses") Collection<ReservationStatus> statuses);

    // ---- Skadimi (scheduled job, 2.7 / 3.x) ----
    List<Reservation> findByStatusAndExpiresAtLessThanEqual(ReservationStatus status,
                                                            OffsetDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Reservation r
           set r.status = com.blerinahouse.entity.enums.ReservationStatus.EXPIRED
           where r.status = com.blerinahouse.entity.enums.ReservationStatus.PENDING
             and r.expiresAt <= :now
           """)
    int expirePendingReservations(@Param("now") OffsetDateTime now);

    // ---- Admin ----
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);
    Page<Reservation> findByRoomId(Long roomId, Pageable pageable);

    // ---- reservation_code: nextval i sekuencës (përdoret te 3.10) ----
    @Query(value = "select nextval('reservation_code_seq')", nativeQuery = true)
    long nextReservationCodeSeq();
}