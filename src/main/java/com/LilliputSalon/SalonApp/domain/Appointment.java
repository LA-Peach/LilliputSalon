package com.LilliputSalon.SalonApp.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
@Getter
@Setter
@Table(name = "Appointment", schema = "dbo")
public class Appointment {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentID")
    private Integer appointmentId;

    @Column(name = "CustomerID", nullable = false)
    private Long customerId;

    @Column(name = "StylistID", nullable = false)
    private Integer stylistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BusinessHoursID", nullable = false)
    private BusinessHours businessHours;

    @Column(name = "ScheduledStartDateTime", nullable = false)
    private LocalDateTime scheduledStartDateTime;

    @Column(name = "DurationMinutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "Status", nullable = false, length = 50)
    private String status;

    @Column(name = "BaseAmount", precision = 18, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "DiscountAmount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "PointsEarned")
    private Integer pointsEarned;

    @Column(name = "IsCompleted")
    private Boolean isCompleted;
    
    @OneToMany(
    	    mappedBy = "appointment",
    	    fetch = FetchType.LAZY,
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true
    	)
    	private List<AppointmentService> appointmentServices;


    public int getTotalDurationMinutes() {
        return durationMinutes != null ? durationMinutes : 0;
    }

    
    


}
