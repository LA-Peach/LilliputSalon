package com.LilliputSalon.SalonApp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Availability", schema = "dbo")
@Getter
@Setter
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AvailabilityID")
    private Integer availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "WorkDate", nullable = false)
    private LocalDate workDate;

    @Column(name = "DayStartTime", nullable = false)
    private LocalTime dayStartTime;

    @Column(name = "DayEndTime", nullable = false)
    private LocalTime dayEndTime;

    @Column(name = "IsAvailable", nullable = false)
    private Boolean isAvailable;

    // One Availability can have multiple Breaks
    @OneToMany(mappedBy = "availability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreakTime> breakTimes;
    
    
}
