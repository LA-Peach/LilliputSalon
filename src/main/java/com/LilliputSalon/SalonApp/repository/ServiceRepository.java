package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.Service;
import com.LilliputSalon.SalonApp.domain.ServiceCategory;

public interface ServiceRepository extends JpaRepository<Service, Long> {

	@Override
	List<Service> findAllById(Iterable<Long> ids);

    List<Service> findByIsAvailableTrue();

    List<Service> findByCategory(ServiceCategory category);

    List<Service> findByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCase(String name);
}