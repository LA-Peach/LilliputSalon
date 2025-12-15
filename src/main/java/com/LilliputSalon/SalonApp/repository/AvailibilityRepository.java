package com.LilliputSalon.SalonApp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.Availability;

public interface AvailibilityRepository extends JpaRepository<Availability, Integer> {

    List<Availability> findByUser_Id(Long stylistId);

    List<Availability> findByWorkDate(LocalDate workDate);

    Availability findByUser_IdAndWorkDate(Long userId, LocalDate workDate);

    Availability findByAvailabilityIdAndUser_Id(Integer availabilityId, Long userId);
    
    @Query("""
    	    select distinct a
    	    from Availability a
    	    left join fetch a.breakTimes
    	""")
    	List<Availability> findAllWithBreaks();


}
