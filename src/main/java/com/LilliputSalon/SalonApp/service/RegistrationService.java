package com.LilliputSalon.SalonApp.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.domain.UserType;
import com.LilliputSalon.SalonApp.security.RegistrationRequest;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.repository.UserTypeRepository;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegistrationRequest request) {

        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already exists.");
        });

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match.");
        }

        UserType customerRole = userTypeRepository.findAll()
            .stream()
            .filter(ut -> ut.getTypeName().equalsIgnoreCase("CUSTOMER"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No CUSTOMER UserType found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // <-- plain text stored here
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUserType(customerRole);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setIsActiveStylist(false);

        profileRepository.save(profile);
    }

}
