package com.LilliputSalon.SalonApp.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.dto.NextAppointmentDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@Controller
public class HomeController {

    private final ProfileRepository profileRepository;
    private final AppointmentManagerService appointmentService;
    private final AppointmentServiceRepository apptServiceRepo;

    public HomeController(
            ProfileRepository profileRepository,
            AppointmentManagerService appointmentService,
            AppointmentServiceRepository apptServiceRepo
    ) {
        this.profileRepository = profileRepository;
        this.appointmentService = appointmentService;
        this.apptServiceRepo = apptServiceRepo;
    }

    @GetMapping("/home")
    public String home(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUser = (CustomUserDetails) auth.getPrincipal();
        User user = customUser.getUser();
        Long userId = user.getId();

        // Load profile and first name
        profileRepository.findByUser_Id(userId)
                .ifPresent(p -> model.addAttribute("firstName", p.getFirstName()));

        boolean isCustomer = customUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        boolean isStylist = customUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STYLIST"));

        /* ===============================
           CUSTOMER — Next Appointment
        =============================== */
        if (isCustomer) {

        	Profile customerProfile = profileRepository.findByUser_Id(userId)
        	        .orElseThrow(() -> new RuntimeException("Profile not found"));

        	Integer customerProfileId = customerProfile.getProfileId().intValue();

        	Appointment next = appointmentService.getNextAppointmentForCustomer(customerProfileId);

            NextAppointmentDTO customerDTO = null;

            if (next != null) {
            	String providerName = profileRepository.findById(next.getStylistId().longValue())
            		    .map(p -> buildDisplayName(p, "Stylist"))
            		    .orElse("Stylist");

            	String serviceName = apptServiceRepo.findWithServiceByAppointmentId(next.getAppointmentId())
            	        .stream()
            	        .findFirst()
            	        .map(s -> s.getService().getName())
            	        .orElse(null);

                customerDTO = new NextAppointmentDTO(
                        next.getScheduledStartDateTime(),
                        null,
                        providerName,
                        serviceName
                );
            }

            model.addAttribute("nextAppointment", customerDTO);
        }

        /* ===============================
           STYLIST — Next Appointment
        =============================== */
        if (isStylist) {

        	Profile profile = profileRepository.findByUser_Id(userId)
        	        .orElseThrow(() -> new RuntimeException("Profile not found"));

        	Integer stylistProfileId = profile.getProfileId().intValue();

        	Appointment nextStylist = appointmentService.getNextAppointmentForStylist(stylistProfileId);

            NextAppointmentDTO stylistDTO = null;

            if (nextStylist != null) {

            	String clientName = profileRepository.findById(nextStylist.getCustomerId().longValue())
            		    .map(p -> buildDisplayName(p, "Client"))
            		    .orElse("Client");

            	String serviceName = apptServiceRepo.findWithServiceByAppointmentId(nextStylist.getAppointmentId())
            	        .stream()
            	        .findFirst()
            	        .map(s -> s.getService().getName())
            	        .orElse(null);


                stylistDTO = new NextAppointmentDTO(
                        nextStylist.getScheduledStartDateTime(),
                        clientName,
                        null,
                        serviceName
                );
            }

            model.addAttribute("nextStylistAppointment", stylistDTO);
        }

        return "home";
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
