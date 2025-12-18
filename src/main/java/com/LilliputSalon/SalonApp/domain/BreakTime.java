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
@Table(name = "break_time")
@Getter @Setter
public class BreakTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "break_id")
    private Long breakId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false)
    private Availability availability;

    @Column(name = "break_start_time", nullable = false)
    private LocalTime breakStartTime;

    @Column(name = "break_end_time", nullable = false)
    private LocalTime breakEndTime;

    @Column(name = "break_type", length = 50)
    private String breakType;
}
