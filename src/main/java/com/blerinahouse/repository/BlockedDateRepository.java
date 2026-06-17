package com.blerinahouse.repository;

import com.blerinahouse.entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {

    List<BlockedDate> findByRoomId(Long roomId);

    // E njëjta formulë half-open: a përplaset intervali i kërkuar me ndonjë bllokim?
    @Query("""
           select (count(b) > 0) from BlockedDate b
           where b.room.id = :roomId
             and b.blockedFrom < :checkOut
             and b.blockedTo > :checkIn
           """)
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    @Query("""
           select b from BlockedDate b
           where b.room.id = :roomId
             and b.blockedFrom < :rangeEnd
             and b.blockedTo > :rangeStart
           order by b.blockedFrom asc
           """)
    List<BlockedDate> findInRange(@Param("roomId") Long roomId,
                                  @Param("rangeStart") LocalDate rangeStart,
                                  @Param("rangeEnd") LocalDate rangeEnd);
}