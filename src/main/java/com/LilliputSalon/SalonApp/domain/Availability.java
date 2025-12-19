package com.LilliputSalon.SalonApp.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "availability")
@Getter
@Setter
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "day_start_time", nullable = false)
    private LocalTime dayStartTime;

    @Column(name = "day_end_time", nullable = false)
    private LocalTime dayEndTime;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    // One Availability can have multiple Breaks
    @OneToMany(mappedBy = "availability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreakTime> breakTimes;


}
