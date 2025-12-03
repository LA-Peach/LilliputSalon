package com.LilliputSalon.SalonApp.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.LilliputSalon.SalonApp.security.CustomUserDetails;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

