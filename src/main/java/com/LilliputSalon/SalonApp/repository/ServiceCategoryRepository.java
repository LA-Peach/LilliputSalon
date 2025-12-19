package com.LilliputSalon.SalonApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.domain.User;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    boolean existsByCategoryNameIgnoreCase(String categoryName);
    
    @Query("""
    	    SELECT DISTINCT c
    	    FROM ServiceCategory c
    	    LEFT JOIN FETCH c.services s
    	    WHERE s.isAvailable = true
    	""")
    	List<ServiceCategory> findAllWithAvailableServices();
    
    boolean existsByCategoryName(String categoryName);

    Optional<ServiceCategory> findByCategoryName(String categoryName);

}