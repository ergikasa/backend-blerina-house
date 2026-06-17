package com.blerinahouse.controller.admin;

import com.blerinahouse.dto.request.RoomRequest;
import com.blerinahouse.dto.response.BlockedDateResponse;
import com.blerinahouse.dto.response.RoomImageResponse;
import com.blerinahouse.dto.response.RoomResponse;
import com.blerinahouse.service.BlockedDateService;
import com.blerinahouse.service.ImageService;
import com.blerinahouse.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;
    private final ImageService imageService;
    private final BlockedDateService blockedDateService;

    @GetMapping
    public List<RoomResponse> list() {
        return roomService.listAll();
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) {
        return roomService.getById(id);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(req));
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest req) {
        return roomService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        roomService.deactivate(id);
    }

    // ---- Upload deri në 5 imazhe (multipart, fusha "files") ----
    @PostMapping(path = "/{roomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<RoomImageResponse>> uploadImages(
            @PathVariable Long roomId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "altText", required = false) String altText) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageService.uploadBatch(roomId, files, altText));
    }

    // ---- Bllokimet e dhomës ----
    @GetMapping("/{roomId}/blocked-dates")
    public List<BlockedDateResponse> blockedDates(@PathVariable Long roomId) {
        return blockedDateService.listForRoom(roomId);
    }
}