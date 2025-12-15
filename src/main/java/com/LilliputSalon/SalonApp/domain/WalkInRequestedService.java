package com.LilliputSalon.SalonApp.domain;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
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
@Table(name = "WalkInRequestedService")
public class WalkInRequestedService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walkInServiceId;

    @ManyToOne
    @JoinColumn(name = "WalkInID")
    private WalkIn walkIn;

    @ManyToOne
    @JoinColumn(name = "ServiceID")
    private Service service;

    private Integer estimatedDurationMinutes;
    private BigDecimal estimatedPrice;
}
