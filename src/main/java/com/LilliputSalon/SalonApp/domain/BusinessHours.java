package com.LilliputSalon.SalonApp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Entity
@Table(name = "Business_Hours", schema = "dbo")
@Getter @Setter
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BusinessHoursID")
    private Integer id;

    @Column(name = "DayOfWeek", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "OpenTime")
    private LocalTime openTime;

    @Column(name = "CloseTime")
    private LocalTime closeTime;

    @Column(name = "IsClosed", nullable = false)
    private Boolean isClosed;
}
