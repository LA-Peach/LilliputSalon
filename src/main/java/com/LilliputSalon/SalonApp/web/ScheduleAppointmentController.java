package com.LilliputSalon.SalonApp.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.ServiceCategoryRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;

@Controller
@PreAuthorize("hasRole('CUSTOMER')")
public class ScheduleAppointmentController {

    private final AppointmentManagerService appointmentService;
    private final ProfileRepository profileRepo;
    private final ServiceRepository serviceRepo;
    private final ServiceCategoryRepository SCrepo;

    public ScheduleAppointmentController(
            AppointmentManagerService appointmentService,
            ProfileRepository profileRepo,
            ServiceRepository serviceRepo,
            ServiceCategoryRepository SCrepo
    ) {
        this.appointmentService = appointmentService;
        this.profileRepo = profileRepo;
        this.serviceRepo = serviceRepo;
        this.SCrepo = SCrepo;
    }

    @GetMapping("/schedule")
    public String showSchedulePage(
            @AuthenticationPrincipal CustomUserDetails user,
            Model model
    ) {
        Profile customerProfile = profileRepo
                .findByUser_Id(user.getUser().getId())
                .orElseThrow();

        LocalDate today = LocalDate.now();

        List<Availability> availableStylists =
                appointmentService.getAllStylistShifts()
                        .stream()
                        .filter(a -> a.getWorkDate().isAfter(today.minusDays(1)))
                        .toList();

        model.addAttribute("customer", customerProfile);
        model.addAttribute("categories",
                SCrepo.findAllWithAvailableServices());
        model.addAttribute("availableStylists", availableStylists);

        return "scheduleAppointment";
    }



}
