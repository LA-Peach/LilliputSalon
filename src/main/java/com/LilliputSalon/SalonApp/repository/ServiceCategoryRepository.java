package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findAllByOrderByDisplayOrderAsc();

    boolean existsByCategoryNameIgnoreCase(String categoryName);
    
    @Query("""
    	    SELECT DISTINCT c
    	    FROM ServiceCategory c
    	    LEFT JOIN FETCH c.services s
    	    WHERE s.isAvailable = true
    	    ORDER BY c.displayOrder
    	""")
    	List<ServiceCategory> findAllWithAvailableServices();

}