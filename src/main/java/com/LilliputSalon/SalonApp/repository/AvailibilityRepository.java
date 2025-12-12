package com.LilliputSalon.SalonApp.repository;

import com.LilliputSalon.SalonApp.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AvailibilityRepository extends JpaRepository<Availability, Integer> {

    List<Availability> findByUser_Id(Long stylistId);

    List<Availability> findByWorkDate(LocalDate workDate);

    Availability findByUser_IdAndWorkDate(Long userId, LocalDate workDate);
    
    Availability findByAvailabilityIdAndUser_Id(Integer availabilityId, Long userId);

}
