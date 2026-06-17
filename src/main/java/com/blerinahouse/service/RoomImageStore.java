package com.blerinahouse.service;

import com.blerinahouse.entity.Room;
import com.blerinahouse.entity.RoomImage;
import com.blerinahouse.exception.ResourceNotFoundException;
import com.blerinahouse.repository.RoomImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomImageStore {

    private final RoomImageRepository roomImageRepository;

    @Transactional
    public RoomImage save(Room room, String publicId, String url, String altText, boolean isCover) {
        if (isCover) unsetCurrentCover(room.getId());
        RoomImage img = new RoomImage();
        img.setRoom(room);
        img.setCloudinaryPublicId(publicId);
        img.setImageUrl(url);
        img.setAltText(altText);
        img.setIsCover(isCover);
        img.setDisplayOrder(nextDisplayOrder(room.getId()));
        return roomImageRepository.save(img);
    }

    @Transactional
    public void setCover(Long imageId) {
        RoomImage img = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: id=" + imageId));
        unsetCurrentCover(img.getRoom().getId());
        roomImageRepository.flush();   // old cover -> false APLIKOHET para new -> true
        img.setIsCover(true);
    }

    @Transactional
    public String deleteAndReturnPublicId(Long imageId) {
        RoomImage img = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: id=" + imageId));
        String publicId = img.getCloudinaryPublicId();
        roomImageRepository.delete(img);
        return publicId;
    }

    private void unsetCurrentCover(Long roomId) {
        roomImageRepository.findByRoomIdAndIsCoverTrue(roomId)
                .ifPresent(c -> c.setIsCover(false));
    }

    private int nextDisplayOrder(Long roomId) {
        return roomImageRepository.findByRoomIdOrderByDisplayOrderAsc(roomId).size();
    }
}