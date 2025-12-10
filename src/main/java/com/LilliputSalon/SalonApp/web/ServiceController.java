package com.LilliputSalon.SalonApp.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.service.ServiceManagerService;

@Controller
@RequestMapping("/services")
public class ServiceController {

    private final ServiceManagerService serviceService;

    public ServiceController(ServiceManagerService serviceService) {
        this.serviceService = serviceService;
    }

    // =============================
    //  PUBLIC LIST VIEW (all roles)
    // =============================
    @GetMapping
    public String listServices(Model model) {
        List<ServiceCategory> categories = serviceService.getAllCategoriesOrdered();
        model.addAttribute("categories", categories);
        return "services";
    }

}
