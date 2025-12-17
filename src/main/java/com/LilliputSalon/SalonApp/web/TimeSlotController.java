package com.LilliputSalon.SalonApp.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.LilliputSalon.SalonApp.dto.TimeSlotDTO;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@RestController
@RequestMapping("/web")
@PreAuthorize("hasRole('CUSTOMER')")
public class TimeSlotController {

    private final AppointmentManagerService appointmentService;

    public TimeSlotController(AppointmentManagerService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/timeslots")
    public List<TimeSlotDTO> getTimeSlots(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long stylistId,
            @RequestParam List<Long> serviceIds
    ) {
        // 🛑 GUARD CLAUSE — MUST BE FIRST
        if (date == null || date.isBlank() || "undefined".equals(date)) {
            return List.of();
        }

        LocalDate selectedDate = LocalDate.parse(date);

        return appointmentService
                .getAvailableTimeSlots(selectedDate, stylistId, serviceIds)
                .stream()
                .map(t -> new TimeSlotDTO(t.toString()))
                .toList();
    }

}