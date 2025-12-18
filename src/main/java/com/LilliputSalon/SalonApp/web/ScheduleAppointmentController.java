package com.LilliputSalon.SalonApp.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.dto.CreateAppointmentDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentRepository;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.ServiceCategoryRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;
import com.LilliputSalon.SalonApp.security.CustomUserDetails;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@Controller

@PreAuthorize("hasRole('CUSTOMER')")
public class ScheduleAppointmentController {

    private final AppointmentManagerService appointmentService;
    private final ProfileRepository profileRepo;
    private final ServiceRepository serviceRepo;
    private final ServiceCategoryRepository SCrepo;
    private final AvailibilityRepository availibilityRepo;
    private final AppointmentRepository repo;


    public ScheduleAppointmentController(
            AppointmentManagerService appointmentService,
            ProfileRepository profileRepo,
            ServiceRepository serviceRepo,
            ServiceCategoryRepository SCrepo,
            AvailibilityRepository availibilityRepo,
            AppointmentRepository repo
    ) {
        this.appointmentService = appointmentService;
        this.profileRepo = profileRepo;
        this.serviceRepo = serviceRepo;
        this.SCrepo = SCrepo;
        this.availibilityRepo = availibilityRepo;
        this.repo = repo;
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
    
    @PostMapping("/schedule/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String createAppointment(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam List<Long> serviceIds,
            @RequestParam(required = false) Long stylistId,
            @RequestParam String date,
            @RequestParam String time,
            Model model
    ) {
        Long customerId = user.getUser().getId();

        LocalDate selectedDate = LocalDate.parse(date);
        LocalTime selectedTime = LocalTime.parse(time);
        LocalDateTime startDateTime = LocalDateTime.of(selectedDate, selectedTime);
        
     // If customer didn't choose a stylist, auto-assign one that can take this slot
        if (stylistId == null) {
            Long assigned = appointmentService.findAvailableStylistForSlot(
                    selectedDate, selectedTime, serviceIds
            );

            if (assigned == null) {
                return reloadWithError(model, user,
                        "No stylists are available at that time. Please choose a different time.");
            }

            stylistId = assigned;
        }

        LocalDateTime now = LocalDateTime.now();

        // ❌ Past date/time
        if (startDateTime.isBefore(now)) {
            return reloadWithError(model, user,
                "You cannot book an appointment in the past.");
        }

        boolean isCustomer = user.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        // ❌ Past date/time (always blocked)
        if (startDateTime.isBefore(now)) {
            return reloadWithError(model, user,
                "You cannot book an appointment in the past.");
        }

        // ⏱️ 15-minute rule ONLY for today
        if (isCustomer && startDateTime.toLocalDate().equals(now.toLocalDate())) {
            if (startDateTime.isBefore(now.plusMinutes(15))) {
                return reloadWithError(model, user,
                    "Appointments today must be booked at least 15 minutes in advance.");
            }
        }


        CreateAppointmentDTO dto = new CreateAppointmentDTO();
        dto.setServiceIds(serviceIds);
        dto.setStylistId(stylistId);
        dto.setStart(
                startDateTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toString()
        );

        appointmentService.create(dto, customerId, null);

        return "redirect:/myAppointments";
    }


    
    private void validateCustomerStartTime(LocalDateTime startDateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startDateTime.isBefore(now)) {
            throw new IllegalArgumentException(
                "Appointments cannot be scheduled in the past."
            );
        }

        if (startDateTime.isBefore(now.plusMinutes(15))) {
            throw new IllegalArgumentException(
                "Appointments must be scheduled at least 15 minutes in advance."
            );
        }
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidationError(
            IllegalArgumentException ex,
            Model model,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Profile customerProfile = profileRepo
                .findByUser_Id(user.getUser().getId())
                .orElseThrow();

        model.addAttribute("customer", customerProfile);
        model.addAttribute("categories", SCrepo.findAllWithAvailableServices());
        model.addAttribute("error", ex.getMessage());

        return "scheduleAppointment";
    }
    
    private String reloadWithError(
            Model model,
            CustomUserDetails user,
            String errorMessage
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
        model.addAttribute("error", errorMessage);

        return "scheduleAppointment";
    }









}
