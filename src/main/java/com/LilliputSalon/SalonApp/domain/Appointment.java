package com.LilliputSalon.SalonApp.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "appointment")
public class Appointment {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "stylist_id", nullable = false)
    private Long stylistId;

    @Column(name = "scheduled_start_datetime", nullable = false)
    private LocalDateTime scheduledStartDateTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "base_amount", precision = 18, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @OneToMany(
    	    mappedBy = "appointment",
    	    fetch = FetchType.LAZY,
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true
    	)
    private List<AppointmentService> appointmentServices;


    public int getTotalDurationMinutes() {
        return durationMinutes != null ? durationMinutes : 0;}

	}


