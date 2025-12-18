package com.LilliputSalon.SalonApp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.AppointmentService;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCustomerId(Long id);

    List<Appointment> findByStylistId(Long stylistId);

    List<Appointment> findByScheduledStartDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStylistIdAndScheduledStartDateTimeBetween(
            Long stylistId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Find the earliest future appointment for this stylist
    Optional<Appointment> findFirstByStylistIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
            Long stylistId,
            LocalDateTime after
    );

    // Next appointment for a customer
    Optional<Appointment> findFirstByCustomerIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
            Long customerId,
            LocalDateTime after
    );

    @Query("""
    	    select distinct a
    	    from Appointment a
    	    left join fetch a.appointmentServices aps
    	    left join fetch aps.service
    	""")
    	List<Appointment> findAllWithServices();

    @Query(value = """
    	    SELECT COUNT(*)
    	    FROM dbo.Appointment a
    	    WHERE a.StylistID = :stylistId
    	      AND a.IsCompleted = 0
    	      AND a.ScheduledStartDateTime < :end
    	      AND DATEADD(MINUTE, a.DurationMinutes, a.ScheduledStartDateTime) > :start
    	      AND (:excludeId IS NULL OR a.AppointmentID <> :excludeId)
    	""", nativeQuery = true)
    	int countOverlappingAppointments(
    	    @Param("stylistId") Long stylistId,
    	    @Param("start") LocalDateTime start,
    	    @Param("end") LocalDateTime end,
    	    @Param("excludeId") Long excludeId
    	);

    
    @Query("""
    	    SELECT a
    	    FROM Appointment a
    	    WHERE a.isCompleted = false
    	""")
    	List<Appointment> findActiveAppointments();
    
    Long countByStylistIdAndIsCompletedTrue(Long stylistId);
    
    @Query("""
    	    SELECT COUNT(a)
    	    FROM Appointment a
    	    WHERE a.scheduledStartDateTime
    	          BETWEEN :start AND :end
    	""")
    	long countAppointmentsToday(
    	        LocalDateTime start,
    	        LocalDateTime end
    	);
    




}
