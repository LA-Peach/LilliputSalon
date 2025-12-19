package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.LilliputSalon.SalonApp.domain.AppointmentService;
import com.LilliputSalon.SalonApp.dto.ServiceCountDTO;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Long> {

	List<AppointmentService> findByAppointment_AppointmentId(Long appointmentId);

	@Query("""
		    SELECT aps
		    FROM AppointmentService aps
		        JOIN FETCH aps.service
		    WHERE aps.appointment.appointmentId = :appointmentId
		""")
		List<AppointmentService> findWithServiceByAppointmentId(
		    @Param("appointmentId") Long appointmentId
		);


	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM AppointmentService aps WHERE aps.appointment.appointmentId = :id")
	void deleteByAppointmentId(@Param("id") Long id);

	public interface TopServiceCount {

	    String getName();   // service name
	    Long getCount();    // number completed
	}

	@Query("""
		    SELECT
		        s.name AS name,
		        COUNT(asv.id) AS count
		    FROM AppointmentService asv
		    JOIN asv.service s
		    JOIN asv.appointment a
		    WHERE a.stylistId = :stylistId
		      AND a.isCompleted = true
		    GROUP BY s.name
		    ORDER BY COUNT(asv.id) DESC
		""")
		List<TopServiceCount> findTopServicesForStylist(Long stylistId);

	@Query("""
	        SELECT COUNT(asvc)
	        FROM AppointmentService asvc
	        JOIN asvc.appointment appt
	        WHERE appt.isCompleted = true
	    """)
	    Long countAllCompletedServices();

	@Query("""
		    SELECT new com.LilliputSalon.SalonApp.dto.ServiceCountDTO(
		        s.name,
		        COUNT(asvc)
		    )
		    FROM AppointmentService asvc
		    JOIN asvc.service s
		    JOIN asvc.appointment appt
		    WHERE appt.isCompleted = true
		    GROUP BY s.name
		    ORDER BY COUNT(asvc) DESC
		""")
		List<ServiceCountDTO> findTopBusinessServices();



}
