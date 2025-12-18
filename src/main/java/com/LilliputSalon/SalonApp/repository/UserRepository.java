package com.LilliputSalon.SalonApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.LilliputSalon.SalonApp.domain.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

	@Query("""
		    SELECT u FROM User u
		    JOIN FETCH u.profile p
		    JOIN FETCH p.userType
		    WHERE u.email = :email
		""")
		Optional<Users> findByEmail(String email);


}
