package com.LilliputSalon.SalonApp.repository;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.LilliputSalon.SalonApp.domain.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByLastNameContainingIgnoreCase(String lastName);
    Optional<Profile> findByUser_Id(Long userId);
    List<Profile> findByIsActiveStylistTrue();

    @Query("""
            select p
            from Profile p
            join p.user u
            where lower(u.email) like lower(concat('%', :q, '%'))
        """)
        List<Profile> searchByEmail(@Param("q") String q);

    @Query("""
    	    select p
    	    from Profile p
    	    where p.user.id in :userIds
    	""")
    	List<Profile> findByUserIds(@Param("userIds") Set<Long> userIds);


}


