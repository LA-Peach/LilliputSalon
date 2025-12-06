package com.LilliputSalon.SalonApp.web;

import com.LilliputSalon.SalonApp.domain.Service;
import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.service.ServiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
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

    // =============================
    //  OWNER MANAGEMENT PAGE
    // =============================
    @GetMapping("/management")
    @PreAuthorize("hasRole('OWNER')")
    public String manageServices(Model model) {
        List<ServiceCategory> categories = serviceService.getAllCategoriesOrdered();
        model.addAttribute("categories", categories);
        return "management";
    }

    // =============================
    //  ADD NEW SERVICE
    // =============================
    @GetMapping("/add")
    @PreAuthorize("hasRole('OWNER')")
    public String showAddForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("categories", serviceService.getAllCategoriesOrdered());
        return "services/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('OWNER')")
    public String addService(@ModelAttribute Service service) {

        // Default new service is active
        service.setIsAvailable(true);

        serviceService.save(service);
        return "redirect:/services/manage?success";
    }

    // =============================
    //  EDIT SERVICE
    // =============================
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String editForm(@PathVariable Long id, Model model) {
        Service service = serviceService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        model.addAttribute("service", service);
        model.addAttribute("categories", serviceService.getAllCategoriesOrdered());

        return "services/edit";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasRole('OWNER')")
    public String saveEdit(@ModelAttribute Service service) {

        Service existing = serviceService.getById(service.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid service ID"));

        existing.setName(service.getName());
        existing.setDescription(service.getDescription());
        existing.setBasePrice(service.getBasePrice());
        existing.setTypicalDurationMinutes(service.getTypicalDurationMinutes());
        existing.setCategory(service.getCategory());

        serviceService.save(existing);

        return "redirect:/services/manage?updated";
    }

    // =============================
    //  DEACTIVATE SERVICE
    // =============================
    @PostMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String deactivate(@PathVariable Long id) {
        serviceService.archive(id);
        return "redirect:/services/manage?deactivated";
    }

}
