package com.blerinahouse.service;

import com.blerinahouse.dto.response.RoomImageResponse;
import com.blerinahouse.entity.Room;
import com.blerinahouse.entity.RoomImage;
import com.blerinahouse.exception.BusinessRuleException;
import com.blerinahouse.exception.ImageStorageException;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.mapper.RoomImageMapper;
import com.blerinahouse.repository.RoomImageRepository;
import com.blerinahouse.repository.RoomRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private static final String FOLDER = "blerina-house/rooms";
    private static final int MAX_IMAGES_PER_ROOM = 5;

    private final Cloudinary cloudinary;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomImageStore store;
    private final RoomImageMapper roomImageMapper;

    // ===== UPLOAD i shumëfishtë (deri 5) =====
    public List<RoomImageResponse> uploadBatch(Long roomId, List<MultipartFile> files, String altText) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: id=" + roomId));

        if (files == null || files.isEmpty()) {
            throw new BusinessRuleException("At least one image is required");
        }
        if (files.size() > MAX_IMAGES_PER_ROOM) {
            throw new BusinessRuleException("You can upload at most " + MAX_IMAGES_PER_ROOM + " images at once");
        }
        long existing = roomImageRepository.countByRoomId(roomId);
        if (existing + files.size() > MAX_IMAGES_PER_ROOM) {
            throw new BusinessRuleException(
                    "A room can have at most " + MAX_IMAGES_PER_ROOM + " images (currently " + existing + ")");
        }

        boolean coverAssigned = roomImageRepository.existsByRoomIdAndIsCoverTrue(roomId);
        List<RoomImageResponse> result = new ArrayList<>();
        for (MultipartFile file : files) {
            boolean makeCover = !coverAssigned;
            result.add(uploadOne(room, file, altText, makeCover));
            if (makeCover) coverAssigned = true;
        }
        return result;
    }

    // ===== UPLOAD njëshe =====
    public RoomImageResponse upload(Long roomId, MultipartFile file, String altText, boolean isCover) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: id=" + roomId));
        if (roomImageRepository.countByRoomId(roomId) + 1 > MAX_IMAGES_PER_ROOM) {
            throw new BusinessRuleException("A room can have at most " + MAX_IMAGES_PER_ROOM + " images");
        }
        return uploadOne(room, file, altText, isCover);
    }

    // ===== një foto =====
    private RoomImageResponse uploadOne(Room room, MultipartFile file, String altText, boolean isCover) {
        validateImage(file);

        // FAZA 1 — Cloudinary (çdo dështim -> 502 me shkakun real)
        String publicId;
        String url;
        try {
            Map<?, ?> res = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", FOLDER, "resource_type", "image"));
            publicId = (String) res.get("public_id");
            url = (String) res.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload IO error", e);
            throw new ImageStorageException("Cloudinary upload failed: " + e.getMessage());
        } catch (RuntimeException e) {
            // p.sh. api_key/secret gabim, cloud_name gabim, URL keqformuar
            log.error("Cloudinary upload error", e);
            throw new ImageStorageException("Cloudinary upload failed: " + e.getMessage());
        }
        if (publicId == null || url == null) {
            throw new ImageStorageException("Cloudinary returned no public_id/secure_url");
        }

        // FAZA 2 — DB (tx te store) + kompensim nëse dështon
        try {
            RoomImage saved = store.save(room, publicId, url, altText, isCover);
            return roomImageMapper.toResponse(saved);
        } catch (RuntimeException ex) {
            log.error("DB save failed after Cloudinary upload; compensating (destroy {})", publicId, ex);
            safeDestroy(publicId);          // fshi asset-in jetim
            throw ex;
        }
    }

    public void delete(Long imageId) {
        String publicId = store.deleteAndReturnPublicId(imageId);
        safeDestroy(publicId);
    }

    public void setCover(Long imageId) {
        store.setCover(imageId);
    }

    private void safeDestroy(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary asset {}: {}", publicId, e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessRuleException("File is empty");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new BusinessRuleException("Only image files are allowed");
        }
    }
}