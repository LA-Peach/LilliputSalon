package com.LilliputSalon.SalonApp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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