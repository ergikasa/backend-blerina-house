package com.blerinahouse.controller.public_;

import com.blerinahouse.dto.response.RoomResponse;
import com.blerinahouse.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomResponse> list() {
        return roomService.listActiveRooms();        // vetëm dhomat aktive
    }

    @GetMapping("/{slug}")
    public RoomResponse getBySlug(@PathVariable String slug) {
        return roomService.getBySlug(slug);
    }
}