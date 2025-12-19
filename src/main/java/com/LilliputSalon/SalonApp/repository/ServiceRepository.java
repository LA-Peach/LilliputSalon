package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.domain.Services;

public interface ServiceRepository extends JpaRepository<Services, Long> {

	@Override
	List<Services> findAllById(Iterable<Long> ids);

    List<Services> findByIsAvailableTrue();

    List<Services> findByCategory(ServiceCategory category);

    List<Services> findByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByName(String name);
}