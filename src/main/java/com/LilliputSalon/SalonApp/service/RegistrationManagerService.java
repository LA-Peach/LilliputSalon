package com.LilliputSalon.SalonApp.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.domain.UserType;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.repository.UserTypeRepository;
import com.LilliputSalon.SalonApp.security.RegistrationRequest;

@Service
public class RegistrationManagerService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationManagerService(
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

        // Create the user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // <-- FIXED
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Create the profile
        Profile profile = new Profile();
        profile.setUser(user); // <-- FIXED: setUser, not setUserId
        profile.setUserType(customerRole);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setIsActiveStylist(false);

        profileRepository.save(profile);
    }

}
