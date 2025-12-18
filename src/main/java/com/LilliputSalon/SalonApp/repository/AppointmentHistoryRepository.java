package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.AppointmentHistory;

public interface AppointmentHistoryRepository extends JpaRepository<AppointmentHistory, Long> {

    List<AppointmentHistory> findByAppointmentId(Long appointmentId);
}
