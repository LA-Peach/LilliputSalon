package com.LilliputSalon.SalonApp.repository;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findAllByOrderByDisplayOrderAsc();

    boolean existsByCategoryNameIgnoreCase(String categoryName);
}