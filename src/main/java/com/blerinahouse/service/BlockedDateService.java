package com.blerinahouse.service;

import com.blerinahouse.dto.request.BlockedDateRequest;
import com.blerinahouse.dto.response.BlockedDateResponse;
import com.blerinahouse.entity.Admin;
import com.blerinahouse.entity.BlockedDate;
import com.blerinahouse.entity.Room;
import com.blerinahouse.entity.enums.ReservationStatus;
import com.blerinahouse.exception.BusinessRuleException;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockedDateService {

    private final BlockedDateRepository blockedDateRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public Long block(BlockedDateRequest req, String adminUsername) {
        if (!req.blockedTo().isAfter(req.blockedFrom())) {
            throw new BusinessRuleException("blockedTo must be after blockedFrom");
        }
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: id=" + req.roomId()));
        Admin admin = adminRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminUsername));

        if (blockedDateRepository.existsOverlap(room.getId(), req.blockedFrom(), req.blockedTo())) {
            throw new BusinessRuleException("Overlapping blocked range for this room");
        }
        if (reservationRepository.existsOverlap(room.getId(), req.blockedFrom(), req.blockedTo(),
                ReservationStatus.BLOCKING)) {
            throw new BusinessRuleException("Cannot block: active reservation exists in this range");
        }

        BlockedDate b = new BlockedDate();
        b.setRoom(room);
        b.setCreatedBy(admin);
        b.setBlockedFrom(req.blockedFrom());
        b.setBlockedTo(req.blockedTo());
        b.setReason(req.reason());
        return blockedDateRepository.save(b).getId();
    }
    @Transactional
    public void unblock(Long id) {
        if (!blockedDateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blocked date not found: id=" + id);
        }
        blockedDateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BlockedDateResponse> listForRoom(Long roomId) {
        return blockedDateRepository.findByRoomId(roomId).stream()
                .map(b -> new BlockedDateResponse(
                        b.getId(), b.getRoom().getId(),
                        b.getBlockedFrom(), b.getBlockedTo(), b.getReason()))
                .toList();
    }
}