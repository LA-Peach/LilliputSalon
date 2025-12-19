package com.LilliputSalon.SalonApp.web;

import java.time.LocalDate;
import java.util.Comparator;
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
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@Controller
@PreAuthorize("hasAnyRole('STYLIST','OWNER')")
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
    public String myDay(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {

        boolean isOwner = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (isOwner) {
            loadOwnerView(model);
        } else {
            loadStylistView(currentUser, model);
        }

        return "myDay";
    }

    /* =========================================================
       STYLIST VIEW (existing behavior, extracted)
       ========================================================= */
    private void loadStylistView(CustomUserDetails currentUser, Model model) {

        Long userId = currentUser.getUser().getId();

        Profile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Long stylistProfileId = profile.getProfileId();

        List<Appointment> apptsThisDay =
                appointmentService.getTodayOrNextForStylist(stylistProfileId);

        // 🔑 KEY FIX: wrap stylist appointments in the same structure
        Map<Profile, List<Appointment>> appointmentsByStylist = new HashMap<>();
        appointmentsByStylist.put(profile, apptsThisDay);

        model.addAttribute("appointmentsByStylist", appointmentsByStylist);
        model.addAttribute("displayDate",
                apptsThisDay.isEmpty()
                        ? LocalDate.now()
                        : apptsThisDay.get(0)
                                .getScheduledStartDateTime()
                                .toLocalDate());

        model.addAttribute("isOwnerView", false);

        attachServicesAndCustomers(apptsThisDay, model);
    }


    /* =========================================================
       OWNER VIEW (all working stylists, grouped)
       ========================================================= */
    private void loadOwnerView(Model model) {

        LocalDate today = LocalDate.now();

        // Only stylists who have availability today
        var availabilities =
                appointmentService.getAllStylistShifts().stream()
                        .filter(a -> a.getWorkDate().equals(today))
                        .toList();

        Map<Profile, List<Appointment>> appointmentsByStylist = new HashMap<>();

        for (var availability : availabilities) {

            Profile stylistProfile = profileRepo
                    .findByUser_Id(availability.getUser().getId())
                    .orElseThrow();

            List<Appointment> appts =
            	    appointmentService.getAppointmentsForStylistOnDate(
            	        availability.getUser().getId(),
            	        today
            	    ).stream()
            	     .sorted(Comparator.comparing(Appointment::getScheduledStartDateTime))
            	     .toList();


            appointmentsByStylist.put(stylistProfile, appts);
        }

        model.addAttribute("appointmentsByStylist", appointmentsByStylist);
        model.addAttribute("displayDate", today);
        model.addAttribute("isToday", true);
        model.addAttribute("isOwnerView", true);

        // flatten for shared helpers
        List<Appointment> allAppointments =
                appointmentsByStylist.values()
                        .stream()
                        .flatMap(List::stream)
                        .toList();

        attachServicesAndCustomers(allAppointments, model);
    }

    /* =========================================================
       SHARED HELPERS
       ========================================================= */
    private void populateCommonModel(
            List<Appointment> apptsThisDay,
            String stylistName,
            Model model
    ) {
        LocalDate today = LocalDate.now();
        LocalDate displayDate = apptsThisDay.isEmpty()
                ? today
                : apptsThisDay.get(0)
                        .getScheduledStartDateTime()
                        .toLocalDate();

        boolean isToday = displayDate.equals(today);

        List<Appointment> upcomingAppointments = apptsThisDay.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsCompleted()))
                .sorted(
                    Comparator.comparing(Appointment::getScheduledStartDateTime)
                )
                .toList();


        List<Appointment> completedAppointments = apptsThisDay.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCompleted()))
                .sorted(
                    Comparator.comparing(Appointment::getScheduledStartDateTime)
                )
                .toList();


        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("completedAppointments", completedAppointments);
        model.addAttribute("displayDate", displayDate);
        model.addAttribute("isToday", isToday);
        model.addAttribute("stylistName", stylistName);

        attachServicesAndCustomers(apptsThisDay, model);
    }

    private void attachServicesAndCustomers(
            List<Appointment> appts,
            Model model
    ) {
        Map<Long, String> customerNames = new HashMap<>();
        Map<Long, List<?>> servicesByAppt = new HashMap<>();

        for (Appointment appt : appts) {

            servicesByAppt.put(
                    appt.getAppointmentId(),
                    apptServiceRepo.findWithServiceByAppointmentId(
                            appt.getAppointmentId()
                    )
            );

            profileRepo.findById(appt.getCustomerId())
                    .ifPresentOrElse(
                            p -> customerNames.put(
                                    appt.getAppointmentId(),
                                    buildDisplayName(p, "Client")
                            ),
                            () -> customerNames.put(
                                    appt.getAppointmentId(),
                                    "Client"
                            )
                    );
        }

        model.addAttribute("servicesByAppt", servicesByAppt);
        model.addAttribute("customerNames", customerNames);
    }

    @PostMapping("/stylist/completeAppointment")
    public String completeAppointment(@RequestParam Long appointmentId) {
        appointmentService.markAppointmentComplete(appointmentId);
        return "redirect:/myDay";
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
