package com.jaehyun.demo.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stores")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Column
    private String address;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column
    private LocalTime openTime;

    @Column
    private LocalTime closeTime;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean deleted;

    @Column
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Reservation> reservations;

}
