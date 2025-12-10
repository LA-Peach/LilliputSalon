package com.LilliputSalon.SalonApp.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Appointment_History", schema = "dbo")
public class AppointmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentHistoryID")
    private Integer appointmentHistoryId;

    @Column(name = "AppointmentID", nullable = false)
    private Integer appointmentId;

    @Column(name = "ModifiedByUserID", nullable = false)
    private Integer modifiedByUserId;

    @Column(name = "ChangedAt", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "Action", length = 50)
    private String action;

    @Column(name = "OldStartDateTime")
    private LocalDateTime oldStartDateTime;

    @Column(name = "NewStartDateTime")
    private LocalDateTime newStartDateTime;

    @Column(name = "Notes", length = 255)
    private String notes;
}
