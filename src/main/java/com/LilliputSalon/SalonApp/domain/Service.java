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
@Table(name = "Service")
@Getter
@Setter
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceCategoryID", nullable = false)
    private ServiceCategory category;

    @Column(name = "ServiceName", nullable = false)
    private String name;

    @Column(name = "ServiceDescription")
    private String description;

    @Column(name = "BasePrice", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "TypicalDurationMinutes", nullable = false)
    private Integer typicalDurationMinutes;

    @Column(name = "IsAvailable", nullable = false)
    private Boolean isAvailable;

}