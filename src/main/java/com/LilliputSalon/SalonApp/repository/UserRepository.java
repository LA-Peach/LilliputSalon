package com.LilliputSalon.SalonApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.User;


public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

}
