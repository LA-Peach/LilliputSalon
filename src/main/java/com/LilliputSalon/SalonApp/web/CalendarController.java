package com.LilliputSalon.SalonApp.web;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

        // List of distinct, soft colors
        List<String> colorPalette = List.of(
            "#ff9999", // light red
            "#ffcc99", // light orange
            "#ffff99", // light yellow
            "#ccffcc", // light green
            "#99ccff", // light blue
            "#cc99ff"  // light purple
        );

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

            // 🎨 Assign stylist-specific color
            int colorIndex = Math.toIntExact(appt.getStylistId() % colorPalette.size());
            ev.put("backgroundColor", colorPalette.get(colorIndex));
            ev.put("borderColor", colorPalette.get(colorIndex));

            events.add(ev);
        }

        return events;
    }
    
    @ResponseBody
    @PostMapping("/calendar/update")
    public Map<String, String> updateEvent(@RequestBody Map<String, String> payload) {

        Integer id = Integer.valueOf(payload.get("id"));

        // Use OffsetDateTime to parse Zulu timestamps
        OffsetDateTime odtStart = OffsetDateTime.parse(payload.get("start"));
        OffsetDateTime odtEnd = payload.get("end") != null
                ? OffsetDateTime.parse(payload.get("end"))
                : null;

        LocalDateTime start = odtStart.toLocalDateTime();
        LocalDateTime end = odtEnd != null ? odtEnd.toLocalDateTime() : null;

        Appointment appt = appointmentService.getById(id);
        appt.setScheduledStartDateTime(start);

        if (end != null) {
            int newDuration = (int) java.time.Duration.between(start, end).toMinutes();
            appt.setDurationMinutes(newDuration);
        }

        appointmentService.save(appt);

        return Map.of("status", "ok");
    }


}
