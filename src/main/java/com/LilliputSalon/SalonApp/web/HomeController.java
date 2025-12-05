package com.LilliputSalon.SalonApp.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;

@Controller
public class HomeController {

    private final ProfileRepository profileRepository;

    public HomeController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping("/home")
    public String home(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // principal is CustomUserDetails, not User
        CustomUserDetails customUser = (CustomUserDetails) auth.getPrincipal();
        User user = customUser.getUser();

        profileRepository.findByUserId(user.getId()).ifPresent(profile ->
            model.addAttribute("firstName", profile.getFirstName())
        );

        return "home";
    }
}
