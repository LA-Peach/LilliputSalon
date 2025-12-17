package com.LilliputSalon.SalonApp.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.LilliputSalon.SalonApp.dto.StylistDTO;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;

@RestController
@RequestMapping("/web")
@PreAuthorize("hasAnyRole('OWNER','STYLIST','CUSTOMER')")
public class StylistController {

    private final ProfileRepository profileRepo;

    public StylistController(ProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping("/stylists")
    public List<StylistDTO> getStylists() {
        return profileRepo.findByIsActiveStylistTrue()
            .stream()
            .map(p -> new StylistDTO(
                p.getUser().getId(),
                p.getFirstName(),
                p.getLastName()
            ))
            .toList();
    }
}
