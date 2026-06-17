package com.blerinahouse.service;

import com.blerinahouse.dto.request.RoomRequest;
import com.blerinahouse.dto.response.RoomResponse;
import com.blerinahouse.entity.Room;
import com.blerinahouse.exception.BusinessRuleException;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.mapper.RoomMapper;
import com.blerinahouse.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional(readOnly = true)
    public List<RoomResponse> listActiveRooms() {
        return roomRepository.findByIsActiveTrueOrderByBasePriceAsc()
                .stream().map(roomMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getBySlug(String slug) {
        Room room = roomRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + slug));
        return roomMapper.toResponse(room);
    }

    @Transactional
    public RoomResponse create(RoomRequest req) {
        Room room = roomMapper.toEntity(req);
        room.setSlug(resolveSlug(req));
        if (room.getIsActive() == null) room.setIsActive(true);
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest req) {
        Room room = getEntity(id);
        roomMapper.updateEntity(req, room);   // dirty checking -> UPDATE automatik
        return roomMapper.toResponse(room);
    }

    @Transactional
    public void deactivate(Long id) {
        getEntity(id).setIsActive(false);     // soft delete (dhomat nuk fshihen)
    }

    @Transactional(readOnly = true)
    public Room getEntity(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: id=" + id));
    }
    @Transactional(readOnly = true)
    public List<RoomResponse> listAll() {
        return roomRepository.findAll().stream().map(roomMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        return roomMapper.toResponse(getEntity(id));
    }

    private String resolveSlug(RoomRequest req) {
        String base = (req.slug() != null && !req.slug().isBlank())
                ? req.slug() : slugify(req.name());
        if (roomRepository.existsBySlug(base)) {
            throw new BusinessRuleException("Slug already exists: " + base);
        }
        return base;
    }

    private static String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}