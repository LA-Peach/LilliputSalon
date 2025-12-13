package com.LilliputSalon.SalonApp.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.LilliputSalon.SalonApp.repository.ProfileRepository;

@RestController
@PreAuthorize("hasAnyRole('OWNER','STYLIST')")
public class CustomerController {

    private final ProfileRepository profileRepo;

    public CustomerController(ProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping("/customers/search")
    @ResponseBody
    public List<Map<String, String>> searchCustomers(@RequestParam String q) {
        return profileRepo.searchByEmail(q)
            .stream()
            .map(p -> Map.of(
                "email", p.getUser().getEmail(),
                "firstName", p.getFirstName(),
                "lastName", p.getLastName(),
                "phone", Optional.ofNullable(p.getPhone()).orElse("")
            ))
            .toList();
    }

}



