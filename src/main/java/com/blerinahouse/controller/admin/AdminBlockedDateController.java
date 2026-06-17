package com.blerinahouse.controller.admin;

import com.blerinahouse.dto.request.BlockedDateRequest;
import com.blerinahouse.service.BlockedDateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/blocked-dates")
@RequiredArgsConstructor
public class AdminBlockedDateController {

    private final BlockedDateService blockedDateService;

    @PostMapping
    public ResponseEntity<Long> block(@Valid @RequestBody BlockedDateRequest req,
                                      @AuthenticationPrincipal UserDetails admin) {
        Long id = blockedDateService.block(req, admin.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable Long id) {
        blockedDateService.unblock(id);
    }
}