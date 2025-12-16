package com.LilliputSalon.SalonApp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    
    List<Availability> findByWorkDateAndIsAvailableTrue(LocalDate workDate);
    
    @Query("""
    	    select a
    	    from Availability a
    	    where a.workDate = :date
    	      and a.isAvailable = true
    	""")
    	List<Availability> findWorkingStylistsToday(LocalDate date);
    
    @Query("""
    	    select min(a.workDate)
    	    from Availability a
    	    where a.workDate >= :today
    	      and a.isAvailable = true
    	""")
    	Optional<LocalDate> findFirstFutureWorkDate(LocalDate today);

}
