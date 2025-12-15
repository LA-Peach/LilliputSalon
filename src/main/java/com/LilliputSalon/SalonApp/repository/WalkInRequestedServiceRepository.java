package com.LilliputSalon.SalonApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.LilliputSalon.SalonApp.domain.WalkInRequestedService;

public interface WalkInRequestedServiceRepository
        extends JpaRepository<WalkInRequestedService, Long> {

    List<WalkInRequestedService> findByWalkIn_WalkInId(Integer walkInId);

    void deleteByWalkIn_WalkInId(Long walkInId);

}
