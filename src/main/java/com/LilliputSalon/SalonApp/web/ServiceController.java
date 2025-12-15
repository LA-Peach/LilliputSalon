package com.LilliputSalon.SalonApp.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    
    @GetMapping("/api")
    @ResponseBody
    public List<Map<String, Object>> getServicesForCalendar() {
        return serviceService.getAllCategoriesOrdered().stream()
            .map(cat -> {
                Map<String, Object> catMap = new java.util.LinkedHashMap<>();
                catMap.put("categoryId", cat.getId());
                catMap.put("categoryName", cat.getCategoryName());

                List<Map<String, Object>> services =
                    cat.getServices() == null ? List.of() :
                    cat.getServices().stream()
                        .filter(s -> Boolean.TRUE.equals(s.getIsAvailable()))
                        .map(s -> {
                            Map<String, Object> svc = new java.util.LinkedHashMap<>();
                            svc.put("serviceId", s.getId());
                            svc.put("name", s.getName());
                            svc.put("typicalDurationMinutes", s.getTypicalDurationMinutes());
                            return svc;
                        })
                        .toList();

                catMap.put("services", services);
                return catMap;
            })
            .toList();
    }





}
