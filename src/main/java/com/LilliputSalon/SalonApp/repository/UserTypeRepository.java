package com.LilliputSalon.SalonApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.UserType;

public interface UserTypeRepository extends JpaRepository<UserType, Long> {
	
	Optional<UserType> findByTypeNameIgnoreCase(String name);

	
}
