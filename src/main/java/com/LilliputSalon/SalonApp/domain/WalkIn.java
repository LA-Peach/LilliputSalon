package com.LilliputSalon.SalonApp.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
@Table(name = "WalkIn")
public class WalkIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walkInId;

    private Long customerId;

    private LocalDateTime timeEntered;

    private Integer estimatedWaitMinutes;

    private Integer assignedStylistId;

    private String status; // WAITING, ASSIGNED, IN_PROGRESS, CONVERTED, CANCELLED

    @OneToMany(mappedBy = "walkIn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalkInRequestedService> services = new ArrayList<>();

}
