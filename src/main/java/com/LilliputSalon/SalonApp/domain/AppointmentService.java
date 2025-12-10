package com.LilliputSalon.SalonApp.domain;

import java.math.BigDecimal;

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
@Getter
@Setter
@Table(name = "Appointment_Service", schema = "dbo")
public class AppointmentService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentServiceID")
    private Integer appointmentServiceId;

    @Column(name = "AppointmentID", nullable = false)
    private Integer appointmentId;

    @Column(name = "ServiceID", nullable = false)
    private Integer serviceId;

    @Column(name = "ActualPrice", precision = 18, scale = 2, nullable = false)
    private BigDecimal actualPrice;

    @Column(name = "ActualDurationMinutes", nullable = false)
    private Integer actualDurationMinutes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID", insertable = false, updatable = false)
    private Service service;

}
