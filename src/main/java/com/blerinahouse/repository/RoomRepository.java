package com.blerinahouse.repository;

import com.blerinahouse.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Listimi publik: vetëm dhomat aktive.
    List<Room> findByIsActiveTrueOrderByBasePriceAsc();
}