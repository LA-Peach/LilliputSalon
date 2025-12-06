package com.LilliputSalon.SalonApp.repository;

import com.LilliputSalon.SalonApp.domain.Service;
import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByIsAvailableTrue();

    List<Service> findByCategory(ServiceCategory category);

    List<Service> findByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCase(String name);
}