package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.AppointmentService;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Integer> {

    List<AppointmentService> findByAppointmentId(Integer appointmentId);
    
    @Query("""
    	    SELECT aps
    	    FROM AppointmentService aps
    	        JOIN FETCH aps.service
    	    WHERE aps.appointmentId = :appointmentId
    	""")
    	List<AppointmentService> findWithServiceByAppointmentId(Integer appointmentId);

}
