package com.LilliputSalon.SalonApp.web;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@PreAuthorize("hasAnyRole('OWNER','STYLIST')")
public class CalendarController {

    private final AppointmentManagerService appointmentService;
    private final ProfileRepository profileRepo;

    public CalendarController(AppointmentManagerService appointmentService,
                              ProfileRepository profileRepo) {
        this.appointmentService = appointmentService;
        this.profileRepo = profileRepo;
    }

    @GetMapping("/calendar")
    public String calendarPage() {
        return "calendar";  // MUST NOT start with "/"
    }

    @ResponseBody
    @GetMapping(value = "/calendar/events", produces = "application/json")
    public List<Map<String, Object>> getEvents() {

        List<Appointment> appts = appointmentService.getAllAppointments();
        Map<Long, Profile> profileCache = new HashMap<>();

        List<Map<String, Object>> events = new ArrayList<>();

        for (Appointment appt : appts) {
            Map<String, Object> ev = new HashMap<>();

            ev.put("id", appt.getAppointmentId());

            var start = appt.getScheduledStartDateTime();
            var end = start.plusMinutes(appt.getDurationMinutes());

            ev.put("start", start.toString());
            ev.put("end", end.toString());

            Profile stylist = profileCache.computeIfAbsent(
                appt.getStylistId().longValue(),
                id -> profileRepo.findById(id).orElse(null)
            );

            Profile customer = profileCache.computeIfAbsent(
                appt.getCustomerId().longValue(),
                id -> profileRepo.findById(id).orElse(null)
            );

            String stylistName = stylist != null ? stylist.getFirstName() : "Stylist";
            String customerName = customer != null ? customer.getFirstName() : "Client";

            ev.put("title", stylistName + " — " + customerName);

            events.add(ev);
        }

        return events;
    }
}
