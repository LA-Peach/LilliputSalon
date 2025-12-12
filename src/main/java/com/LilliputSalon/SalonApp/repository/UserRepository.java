package com.LilliputSalon.SalonApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("""
		    SELECT u FROM User u
		    JOIN FETCH u.profile p
		    JOIN FETCH p.userType
		    WHERE u.email = :email
		""")
		Optional<User> findByEmail(String email);


}
