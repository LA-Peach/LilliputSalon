package com.LilliputSalon.SalonApp.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.BusinessHours;


public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Integer> {

	BusinessHours findByDayOfWeek(Integer dayOfWeek);

}
