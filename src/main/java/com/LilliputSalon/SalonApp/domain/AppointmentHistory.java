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
@Table(name = "appointment_history")
public class AppointmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_history_id")
    private Long appointmentHistoryId;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "modified_by_user_id", nullable = false)
    private Long modifiedByUserId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "old_start_datetime")
    private LocalDateTime oldStartDateTime;

    @Column(name = "new_start_datetime")
    private LocalDateTime newStartDateTime;

    @Column(name = "Notes", length = 255)
    private String notes;
}
