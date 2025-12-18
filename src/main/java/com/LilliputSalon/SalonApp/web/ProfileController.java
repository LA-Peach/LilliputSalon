package com.LilliputSalon.SalonApp.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.Users;
import com.LilliputSalon.SalonApp.domain.UserType;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.repository.UserTypeRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final UserTypeRepository userTypeRepo;

    public ProfileController(ProfileRepository profileRepo,
                             UserRepository userRepo,
                             UserTypeRepository userTypeRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
        this.userTypeRepo = userTypeRepo;
    }

    @GetMapping("/{userId}")
    public String loadProfile(
        @PathVariable Long userId,
        @AuthenticationPrincipal CustomUserDetails currentUser,
        Model model
    ) {
        Long loggedInUserId = currentUser.getUser().getId();

        if (!loggedInUserId.equals(userId)) {
            throw new AccessDeniedException("Unauthorized");
        }

        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find existing profile or build a default one
        Profile profile = profileRepo.findByUser_Id(userId)
                .orElseGet(() -> {
                    Profile p = new Profile();
                    p.setUser(user);

                    UserType defaultType = userTypeRepo.findAll()
                            .stream()
                            .filter(ut -> ut.getTypeName().equalsIgnoreCase("CUSTOMER"))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No CUSTOMER role found"));

                    p.setUserType(defaultType);
                    return p;
                });

        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping
    public String myProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        return "redirect:/profile/" + userId;
    }



    @PostMapping("/save")
    public String saveProfile(
            @Valid @ModelAttribute("profile") Profile formProfile,
            BindingResult result,
            Model model,
            Authentication auth) {

        // Ensure logged-in user only edits own profile
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = principal.getUser().getId();

        if (!formProfile.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to edit this profile");
        }

        if (result.hasErrors()) {
            model.addAttribute("profile", formProfile);
            return "profile";
        }

        Profile existing = profileRepo.findById(formProfile.getProfileId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Lock user & userType so the form cannot overwrite them
        existing.setUser(existing.getUser());
        existing.setUserType(existing.getUserType());

        // Editable fields only
        existing.setFirstName(formProfile.getFirstName());
        existing.setLastName(formProfile.getLastName());
        existing.setPhone(formProfile.getPhone());
        existing.setHairType(formProfile.getHairType());
        existing.setHairLength(formProfile.getHairLength());
        existing.setPreferences(formProfile.getPreferences());
        existing.setIsActiveStylist(formProfile.getIsActiveStylist());

        profileRepo.save(existing);

        return "redirect:/profile/" + currentUserId + "?success=true";
    }

}
