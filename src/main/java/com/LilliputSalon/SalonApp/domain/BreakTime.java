package com.LilliputSalon.SalonApp.domain;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
