package com.blerinahouse.service;

import com.blerinahouse.dto.request.LoginRequest;
import com.blerinahouse.dto.response.LoginResponse;
import com.blerinahouse.entity.Admin;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.repository.AdminRepository;
import com.blerinahouse.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AdminRepository adminRepository;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        // hedh AuthenticationException (BadCredentials/Disabled) -> 401 te handler-i (3.16)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        Admin admin = adminRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        admin.setLastLoginAt(OffsetDateTime.now());   // dirty checking -> UPDATE

        String token = jwtService.generateToken(admin.getUsername(), admin.getRole());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(),
                admin.getUsername(), admin.getRole());
    }
}