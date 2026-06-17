package com.blerinahouse.repository;

import com.blerinahouse.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    // email NUK është unique (repeat customers) -> kthen listë.
    List<Guest> findByEmailIgnoreCase(String email);
}