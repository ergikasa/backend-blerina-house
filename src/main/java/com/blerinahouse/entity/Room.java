package com.blerinahouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 140)
    private String slug;

    // O4: tekst i lirë me qëllim (JO enum)
    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "size_sqm")
    private Integer sizeSqm;

    @Column(name = "bed_configuration", length = 120)
    private String bedConfiguration;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Aggregate natyror: galeria e dhomës. Inverse side -> read-only navigim.
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<RoomImage> images = new ArrayList<>();

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}