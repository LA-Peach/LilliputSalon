package com.LilliputSalon.SalonApp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Service_Category")
@Getter
@Setter
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceCategoryID")
    private Long id;

    @Column(name = "CategoryName", nullable = false)
    private String categoryName;

    @Column(name = "[Description]")
    private String description;

    @Column(name = "DisplayOrder")
    private Integer displayOrder;

    // FIXED: correct import now
    @OneToMany(mappedBy = "category")
    private List<Service> services;
}
