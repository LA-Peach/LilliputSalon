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
            @AuthenticationPrincipal CustomUserDetails user,
            Model model
    ) {

        Profile profile = profileRepo.findByUser_Id(user.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        List<Appointment> appointments =
                appointmentService.getByCustomer(profile.getProfileId())
                        .stream()
                        .sorted((a, b) ->
                                a.getScheduledStartDateTime()
                                        .compareTo(b.getScheduledStartDateTime()))
                        .toList();

        Map<Integer, String> stylistNames = new HashMap<>();
        Map<Integer, List<?>> servicesByAppt = new HashMap<>();

        for (Appointment appt : appointments) {

            // stylist name
            profileRepo.findById(appt.getStylistId().longValue())
                    .ifPresent(p ->
                            stylistNames.put(
                                    appt.getAppointmentId(),
                                    buildDisplayName(p, "Stylist")
                            )
                    );

            // services
            servicesByAppt.put(
                    appt.getAppointmentId(),
                    ASrepo.findWithServiceByAppointmentId(
                            appt.getAppointmentId()
                    )
            );
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("stylistNames", stylistNames);
        model.addAttribute("servicesByAppt", servicesByAppt);

        return "myAppointments";
    }
    
    private String buildDisplayName(Profile p, String fallback) {
        if (p == null) {
            return fallback;
        }

        String first = p.getFirstName();
        String last = p.getLastName();

        if (first != null && !first.isBlank()
                && last != null && !last.isBlank()) {
            return first + " " + last;
        }

        if (first != null && !first.isBlank()) {
            return first;
        }

        return fallback;
    }


}
