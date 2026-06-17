package com.blerinahouse.repository;

import com.blerinahouse.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    List<RoomImage> findByRoomIdOrderByDisplayOrderAsc(Long roomId);

    Optional<RoomImage> findByRoomIdAndIsCoverTrue(Long roomId);

    long countByRoomId(Long roomId);

    boolean existsByRoomIdAndIsCoverTrue(Long roomId);
}