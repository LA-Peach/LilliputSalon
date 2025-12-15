package com.LilliputSalon.SalonApp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.WalkIn;

public interface WalkInRepository extends JpaRepository<WalkIn, Integer> {

    // All active walk-ins (queue view)
    List<WalkIn> findByStatusOrderByTimeEnteredAsc(String status);

    // Walk-ins for a specific stylist
    List<WalkIn> findByAssignedStylistIdAndStatusOrderByTimeEnteredAsc(
        Integer stylistId,
        String status
    );

    // Walk-ins created today
    @Query("""
        select w
        from WalkIn w
        where w.timeEntered >= :start
          and w.timeEntered < :end
        order by w.timeEntered asc
    """)
    List<WalkIn> findWalkInsForDay(
        LocalDateTime start,
        LocalDateTime end
    );

    // Load walk-in with requested services (important)
    @Query("""
    	    select distinct w
    	    from WalkIn w
    	    left join fetch w.services
    	    where w.walkInId = :walkInId
    	""")
    	WalkIn findWithServices(Integer walkInId);




}
