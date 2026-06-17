package com.blerinahouse.security;

import com.blerinahouse.entity.Admin;
import com.blerinahouse.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPasswordHash())          // BCrypt hash nga DB
                .roles(admin.getRole())                     // "ADMIN" -> ROLE_ADMIN
                .disabled(Boolean.FALSE.equals(admin.getIsActive()))
                .build();
    }
}