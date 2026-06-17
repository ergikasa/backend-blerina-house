package com.blerinahouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "blocked_date")
@Getter
@Setter
@NoArgsConstructor
public class BlockedDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Admin createdBy;

    @Column(name = "blocked_from", nullable = false)
    private LocalDate blockedFrom;

    @Column(name = "blocked_to", nullable = false)
    private LocalDate blockedTo;

    @Column(name = "reason", length = 255)
    private String reason;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}