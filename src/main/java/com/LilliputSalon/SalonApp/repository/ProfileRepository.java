package com.LilliputSalon.SalonApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByLastNameContainingIgnoreCase(String lastName);
    Optional<Profile> findByUserId(Long userId);
}