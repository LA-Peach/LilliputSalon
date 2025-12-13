package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.LilliputSalon.SalonApp.domain.AppointmentService;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Integer> {

	List<AppointmentService> findByAppointment_AppointmentId(Integer appointmentId);

	@Query("""
		    SELECT aps
		    FROM AppointmentService aps
		        JOIN FETCH aps.service
		    WHERE aps.appointment.appointmentId = :appointmentId
		""")
		List<AppointmentService> findWithServiceByAppointmentId(
		    @Param("appointmentId") Integer appointmentId
		);


	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM AppointmentService aps WHERE aps.appointment.appointmentId = :id")
	void deleteByAppointmentId(@Param("id") Integer id);




}
