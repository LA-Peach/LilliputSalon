package com.LilliputSalon.SalonApp.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.LilliputSalon.SalonApp.domain.Services;
import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.service.ServiceManagerService;

@Controller
@PreAuthorize("hasRole('OWNER')")
public class ServiceManagementController {

    private final ServiceManagerService serviceService;

    public ServiceManagementController(ServiceManagerService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping("/serviceManagement")
    public String manageServices(Model model) {
        model.addAttribute("categories", serviceService.getAllCategories());
        return "serviceManagement";
    }

    @PostMapping("/serviceManagement/add")
    @PreAuthorize("hasRole('OWNER')")
    public String saveNewService(
            @RequestParam Long categoryId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal basePrice,
            @RequestParam Integer typicalDurationMinutes
    ) {

        ServiceCategory category = serviceService.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category"));

        Services service = new Services();
        service.setCategory(category);
        service.setName(name);
        service.setDescription(description);
        service.setBasePrice(basePrice);
        service.setTypicalDurationMinutes(typicalDurationMinutes);
        service.setIsAvailable(true);

        serviceService.save(service);

        return "redirect:/serviceManagement?added";
    }

    @GetMapping("/serviceManagement/get/{id}")
    @ResponseBody
    public Map<String, Object> getService(@PathVariable Long id) {

        Services service = serviceService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        Map<String, Object> data = new HashMap<>();
        data.put("id", service.getId());
        data.put("name", service.getName());
        data.put("description", service.getDescription());
        data.put("basePrice", service.getBasePrice());
        data.put("typicalDurationMinutes", service.getTypicalDurationMinutes());
        data.put("categoryId", service.getCategory().getId());

        return data;
    }

    @PostMapping("/serviceManagement/edit")
    public String editService(
            @RequestParam Long id,
            @RequestParam Long categoryId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal basePrice,
            @RequestParam Integer typicalDurationMinutes
    ) {

        Services service = serviceService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service ID"));

        ServiceCategory category = serviceService.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category"));

        service.setName(name);
        service.setDescription(description);
        service.setBasePrice(basePrice);
        service.setTypicalDurationMinutes(typicalDurationMinutes);
        service.setCategory(category);

        serviceService.save(service);

        return "redirect:/serviceManagement?updated";
    }


    @PostMapping("/serviceManagement/deactivate/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String deactivateService(@PathVariable Long id) {
        serviceService.archive(id);
        return "redirect:/serviceManagement?deactivated";
    }

    @PostMapping("/serviceManagement/activate/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public String activateService(@PathVariable Long id) {
        serviceService.unarchive(id);
        return "redirect:/serviceManagement?activated";
    }



}