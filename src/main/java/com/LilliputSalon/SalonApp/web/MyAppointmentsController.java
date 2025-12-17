package com.LilliputSalon.SalonApp.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;

@Controller
@PreAuthorize("hasRole('CUSTOMER')")
public class MyAppointmentsController {

    private final AppointmentManagerService appointmentService;
    private final ProfileRepository profileRepo;
    private final AppointmentServiceRepository ASrepo;
    

    public MyAppointmentsController(
            AppointmentManagerService appointmentService,
            ProfileRepository profileRepo,
            AppointmentServiceRepository ASrepo
    ) {
        this.appointmentService = appointmentService;
        this.profileRepo = profileRepo;
        this.ASrepo = ASrepo;
    }

    @GetMapping("/myAppointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String viewAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Long userId = userDetails.getUser().getId(); // ✅ this matches Appointment.CustomerID

        List<Appointment> allAppointments = appointmentService.getByCustomer(userId);

        LocalDateTime now = LocalDateTime.now();

        List<Appointment> upcomingAppointments = allAppointments.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsCompleted()))
                .filter(a -> a.getScheduledStartDateTime().isAfter(now))
                .sorted((a, b) -> a.getScheduledStartDateTime().compareTo(b.getScheduledStartDateTime()))
                .toList();

        List<Appointment> pastAppointments = allAppointments.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCompleted())
                        || a.getScheduledStartDateTime().isBefore(now))
                .sorted((a, b) -> b.getScheduledStartDateTime().compareTo(a.getScheduledStartDateTime()))
                .toList();

        Map<Integer, String> stylistNames = new HashMap<>();
        Map<Integer, List<?>> servicesByAppt = new HashMap<>();

        for (Appointment appt : allAppointments) {
            profileRepo.findById(appt.getStylistId())
                    .ifPresent(p -> stylistNames.put(
                            appt.getAppointmentId(),
                            (p.getFirstName() + " " + p.getLastName()).trim()
                    ));

            servicesByAppt.put(
                    appt.getAppointmentId(),
                    ASrepo.findWithServiceByAppointmentId(appt.getAppointmentId())
            );
        }

        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("pastAppointments", pastAppointments);
        model.addAttribute("stylistNames", stylistNames);
        model.addAttribute("servicesByAppt", servicesByAppt);

        return "myAppointments";
    }
    
    @PostMapping("/myAppointments/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String cancelAppointment(
            @RequestParam Integer appointmentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        appointmentService.cancelAppointment(
                appointmentId,
                user.getUser().getId()
        );

        return "redirect:/myAppointments";
    }



}
