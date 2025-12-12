package com.LilliputSalon.SalonApp.web;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@Controller
@PreAuthorize("hasRole('STYLIST')")
public class MyDayController {

    private final AppointmentManagerService appointmentService;
    private final ProfileRepository profileRepo;
    private final AppointmentServiceRepository apptServiceRepo;

    public MyDayController(
            AppointmentManagerService appointmentService,
            ProfileRepository profileRepo,
            AppointmentServiceRepository apptServiceRepo
    ) {
        this.appointmentService = appointmentService;
        this.profileRepo = profileRepo;
        this.apptServiceRepo = apptServiceRepo;
    }

    @GetMapping("/myDay")
    public String myDay(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {

        Long userId = currentUser.getUser().getId();

        Profile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Integer stylistProfileId = profile.getProfileId().intValue();

        List<Appointment> apptsThisDay =
                appointmentService.getTodayOrNextForStylist(stylistProfileId);

        LocalDate today = LocalDate.now();
        LocalDate displayDate = apptsThisDay.isEmpty()
                ? today
                : apptsThisDay.get(0).getScheduledStartDateTime().toLocalDate();

        boolean isToday = displayDate.equals(today);

        List<Appointment> upcomingAppointments = apptsThisDay.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsCompleted()))
                .toList();

        List<Appointment> completedAppointments = apptsThisDay.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCompleted()))
                .toList();

        var servicesByAppt = apptsThisDay.stream()
                .collect(Collectors.toMap(
                        Appointment::getAppointmentId,
                        a -> apptServiceRepo.findWithServiceByAppointmentId(a.getAppointmentId())
                ));

        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("completedAppointments", completedAppointments);
        model.addAttribute("servicesByAppt", servicesByAppt);
        model.addAttribute("stylistName", profile.getFirstName());
        model.addAttribute("displayDate", displayDate);
        model.addAttribute("isToday", isToday);


        Map<Integer, String> customerNames = new HashMap<>();

        for (Appointment appt : apptsThisDay) {
            profileRepo.findById(appt.getCustomerId().longValue())
                .ifPresentOrElse(
                    p -> customerNames.put(appt.getAppointmentId(), buildDisplayName(p, "Client")),
                    () -> customerNames.put(appt.getAppointmentId(), "Client")
                );
        }

        model.addAttribute("customerNames", customerNames);


        return "myDay";
    }

    @PostMapping("/stylist/completeAppointment")
    public String completeAppointment(@RequestParam Integer appointmentId) {

        appointmentService.markAppointmentComplete(appointmentId);

        return "redirect:/myDay";
    }


    private String buildDisplayName(Profile p, String fallback) {
        if (p == null) {
			return fallback;
		}

        String first = p.getFirstName();
        String last = p.getLastName();

        if (first != null && !first.isBlank() && last != null && !last.isBlank()) {
            return first + " " + last;
        }
        if (first != null && !first.isBlank()) {
            return first;
        }
        return fallback;
    }



}
