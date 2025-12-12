package com.LilliputSalon.SalonApp.repository;

import com.LilliputSalon.SalonApp.domain.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Integer> {

    BusinessHours findByDayOfWeek(Integer dayOfWeek);
}
