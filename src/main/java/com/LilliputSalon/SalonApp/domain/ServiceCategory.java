package com.LilliputSalon.SalonApp.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
