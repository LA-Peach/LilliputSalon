package com.LilliputSalon.SalonApp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.LilliputSalon.SalonApp.domain.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByCustomerId(Integer customerId);

    List<Appointment> findByStylistId(Integer stylistId);

    List<Appointment> findByScheduledStartDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStylistIdAndScheduledStartDateTimeBetween(
            Integer stylistId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Find the earliest future appointment for this stylist
    Optional<Appointment> findFirstByStylistIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
            Integer stylistId,
            LocalDateTime after
    );

    // Next appointment for a customer
    Optional<Appointment> findFirstByCustomerIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
            Integer customerId,
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
    	      AND a.ScheduledStartDateTime < :end
    	      AND DATEADD(MINUTE, a.DurationMinutes, a.ScheduledStartDateTime) > :start
    	      AND (:excludeId IS NULL OR a.AppointmentID <> :excludeId)
    	""", nativeQuery = true)
    	int countOverlappingAppointments(
    	    @Param("stylistId") Integer stylistId,
    	    @Param("start") LocalDateTime start,
    	    @Param("end") LocalDateTime end,
    	    @Param("excludeId") Integer excludeId
    	);




}
