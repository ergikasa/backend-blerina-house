package com.blerinahouse.repository;

import com.blerinahouse.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);   // login (3.13)

    Optional<Admin> findByEmail(String email);

    boolean existsByUsername(String username);
}