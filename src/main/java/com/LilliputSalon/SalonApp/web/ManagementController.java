package com.LilliputSalon.SalonApp.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('OWNER')")
public class ManagementController {

    @GetMapping("/management")
    public String managementDashboard() {
        return "management";
    }

}
