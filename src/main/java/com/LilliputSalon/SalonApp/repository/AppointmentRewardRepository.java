package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.AppointmentReward;

public interface AppointmentRewardRepository extends JpaRepository<AppointmentReward, Integer> {

    List<AppointmentReward> findByAppointmentId(Integer appointmentId);
}
