package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.LilliputSalon.SalonApp.domain.BreakTime;

@Repository
public interface BreakTimeRepository extends JpaRepository<BreakTime, Long> {

    // valid — find all breaks for one availability block
    List<BreakTime> findByAvailability_AvailabilityId(Long availabilityId);

    List<BreakTime> findByAvailability_User_Id(Long userId);

}
