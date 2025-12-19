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
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCustomerId(Long id);

    List<Appointment> findByStylistId(Long stylistId);

    List<Appointment> findByScheduledStartDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByStylistIdAndScheduledStartDateTimeBetween(
            Long stylistId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Appointment> findFirstByStylistIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
            Long stylistId,
            LocalDateTime after
    );

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
        FROM appointment a
        WHERE a.stylist_id = :stylistId
          AND a.is_completed = false
          AND a.scheduled_start_datetime < :end
          AND (a.scheduled_start_datetime + (a.duration_minutes || ' minutes')::interval) > :start
          AND (:excludeId IS NULL OR a.appointment_id <> :excludeId)
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
        WHERE a.scheduledStartDateTime BETWEEN :start AND :end
    """)
    long countAppointmentsToday(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

