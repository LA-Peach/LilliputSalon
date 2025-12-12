package com.LilliputSalon.SalonApp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "BreakTime", schema = "dbo")
@Getter @Setter
public class BreakTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BreakID")
    private Integer breakId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AvailabilityID", nullable = false)
    private Availability availability;

    @Column(name = "BreakStartTime", nullable = false)
    private LocalTime breakStartTime;

    @Column(name = "BreakEndTime", nullable = false)
    private LocalTime breakEndTime;

    @Column(name = "BreakType", length = 50)
    private String breakType;
}
