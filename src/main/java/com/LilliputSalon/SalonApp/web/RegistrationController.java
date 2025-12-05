package com.LilliputSalon.SalonApp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.LilliputSalon.SalonApp.security.RegistrationRequest;
import com.LilliputSalon.SalonApp.service.RegistrationService;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("form", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @ModelAttribute("form") RegistrationRequest form,
            Model model
    ) {
        try {
            registrationService.register(form);
            return "redirect:/login?registered";
        }
        catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }
}
