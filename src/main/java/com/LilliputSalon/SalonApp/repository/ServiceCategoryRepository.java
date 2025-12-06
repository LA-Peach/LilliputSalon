package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findAllByOrderByDisplayOrderAsc();

    boolean existsByCategoryNameIgnoreCase(String categoryName);
}