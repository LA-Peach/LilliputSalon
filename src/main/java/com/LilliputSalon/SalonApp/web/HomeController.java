package com.LilliputSalon.SalonApp.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BreakTime;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.Users;
import com.LilliputSalon.SalonApp.dto.NextAppointmentDTO;
import com.LilliputSalon.SalonApp.dto.WaitTimeDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@Controller
public class HomeController {

    private final ProfileRepository profileRepo;
    private final AppointmentManagerService appointmentService;
    private final AppointmentServiceRepository apptServiceRepo;

    public HomeController(
            ProfileRepository profileRepo,
            AppointmentManagerService appointmentService,
            AppointmentServiceRepository apptServiceRepo
    ) {
        this.profileRepo = profileRepo;
        this.appointmentService = appointmentService;
        this.apptServiceRepo = apptServiceRepo;
    }

    @GetMapping("/home")
    public String home(Model model) {

        CustomUserDetails userDetails = getCurrentUser();
        Users user = userDetails.getUser();
        Long userId = user.getId();

        Profile profile = profileRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        model.addAttribute("firstName", profile.getFirstName());
        
        model.addAttribute(
                "walkInWait",
                appointmentService.calculateWalkInWaitTime()
            );
        
        boolean isCustomer = hasRole(userDetails, "ROLE_CUSTOMER");
        boolean isStylist = hasRole(userDetails, "ROLE_STYLIST");
        boolean isOwner = hasRole(userDetails, "ROLE_OWNER");

        if (isCustomer) {
            loadCustomerDashboard(profile, model);
        }

        if (isStylist) {
            loadStylistDashboard(profile, model);
        }
        
        if (isOwner) {
        	loadOwnerDashboard(profile,model);
        }

        return "home";
    }

    /* =========================================================
       CUSTOMER DASHBOARD
       ========================================================= */
    private void loadCustomerDashboard(Profile customerProfile, Model model) {

        Appointment next = appointmentService
                .getNextAppointmentForCustomer(customerProfile.getProfileId());

        model.addAttribute(
                "nextAppointment",
                buildCustomerAppointmentDTO(next)
        );
    }

    private NextAppointmentDTO buildCustomerAppointmentDTO(Appointment appt) {

        if (appt == null) {
            return null;
        }

        String stylistName = profileRepo
                .findById(appt.getStylistId().longValue())
                .map(p -> buildDisplayName(p, "Stylist"))
                .orElse("Stylist");

        String serviceName = getPrimaryServiceName(appt);

        return new NextAppointmentDTO(
                appt.getScheduledStartDateTime(),
                null,
                stylistName,
                serviceName
        );
    }

    /* =========================================================
       STYLIST DASHBOARD
       ========================================================= */
    private void loadStylistDashboard(Profile stylistProfile, Model model) {

        Appointment next = appointmentService
                .getNextAppointmentForStylist(stylistProfile.getProfileId());

        model.addAttribute(
                "nextStylistAppointment",
                buildStylistAppointmentDTO(next)
        );

        // ✅ NEW: availability for TODAY (by UserID)
        Long userId = stylistProfile.getUser().getId();   // Profile -> User -> id
        Availability today = appointmentService.getAvailabilityForUserOnDate(userId, LocalDate.now());

        model.addAttribute("stylistAvailability", formatAvailability(today));
        
        long completedCount =
                appointmentService.getCompletedAppointmentCountForStylist(
                        stylistProfile.getProfileId()
                );

        model.addAttribute("completedServicesCount", completedCount);
        
        model.addAttribute(
                "topServiceTypes",
                appointmentService.getTopServicesForStylist(
                        stylistProfile.getProfileId()
                )
        );


    }


    private NextAppointmentDTO buildStylistAppointmentDTO(Appointment appt) {

        if (appt == null) {
            return null;
        }

        String clientName = profileRepo
                .findById(appt.getCustomerId().longValue())
                .map(p -> buildDisplayName(p, "Client"))
                .orElse("Client");

        String serviceName = getPrimaryServiceName(appt);

        return new NextAppointmentDTO(
                appt.getScheduledStartDateTime(),
                clientName,
                null,
                serviceName
        );
    }
    
    /* =========================================================
    	OWNER DASHBOARD
    ========================================================= */
    private void loadOwnerDashboard(Profile stylistProfile, Model model) {
    	model.addAttribute(
                "appointmentsToday",
                appointmentService.getAppointmentsTodayCount()
        );
    	model.addAttribute(
    	        "stylistDayViews",
    	        appointmentService.getOwnerDayView()
    	    );
    	model.addAttribute(
    	        "totalServicesCompleted",
    	        apptServiceRepo.countAllCompletedServices()
    	    );
    	model.addAttribute(
    	        "topBusinessServices",
    	        apptServiceRepo.findTopBusinessServices()
    	    );
    
    }
    

    /* =========================================================
       SHARED HELPERS
       ========================================================= */
    private String getPrimaryServiceName(Appointment appt) {
        return apptServiceRepo
                .findWithServiceByAppointmentId(appt.getAppointmentId())
                .stream()
                .findFirst()
                .map(s -> s.getService().getName())
                .orElse(null);
    }

    private boolean hasRole(CustomUserDetails user, String role) {
        return user.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException("Unauthenticated access");
        }

        return (CustomUserDetails) auth.getPrincipal();
    }

    private String buildDisplayName(Profile p, String fallback) {
        if (p == null) return fallback;

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
    
    private String formatAvailability(Availability a) {
        if (a == null || !Boolean.TRUE.equals(a.getIsAvailable())) return null;

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("h:mm a");

        String base = a.getDayStartTime().format(tf) + " – " + a.getDayEndTime().format(tf);

        if (a.getBreakTimes() == null || a.getBreakTimes().isEmpty()) {
            return base;
        }

        String breaks = a.getBreakTimes().stream()
                .sorted(Comparator.comparing(BreakTime::getBreakStartTime))
                .map(b -> {
                    String label = (b.getBreakType() == null || b.getBreakType().isBlank())
                            ? "Break"
                            : b.getBreakType();
                    return label + " " +
                            b.getBreakStartTime().format(tf) + "–" + b.getBreakEndTime().format(tf);
                })
                .reduce((x, y) -> x + ", " + y)
                .orElse("");

        return base + " (" + breaks + ")";
    }
    
    
    
    
}
