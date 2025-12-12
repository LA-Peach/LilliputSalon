package com.LilliputSalon.SalonApp.web;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.dto.CalendarEventDTO;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    @GetMapping(value = "/calendar/shifts", produces = "application/json")
    public List<Map<String, Object>> getShiftEvents() {

        List<Map<String, Object>> events = new ArrayList<>();
        List<Availability> shifts = appointmentService.getAllStylistShifts();
        Map<Long, String> stylistNameCache = new HashMap<>();


        List<String> colorPalette = List.of(
            "#FF7070",
            "#FFB870",
            "#FFFF70",
            "#85FF85",
            "#70B8FF",
            "#B870FF"
        );

        for (Availability shift : shifts) {

            LocalDate date = shift.getWorkDate();

            LocalDateTime start = LocalDateTime.of(date, shift.getDayStartTime());
            LocalDateTime end   = LocalDateTime.of(date, shift.getDayEndTime());

            Long stylistId = shift.getUser().getId();

            String stylistName = stylistNameCache.computeIfAbsent(stylistId, id ->
                profileRepo.findByUser_Id(id)
                    .map(Profile::getFirstName)
                    .orElse("Stylist")
            );


            int colorIndex = (int) (stylistId % colorPalette.size());
            String color = colorPalette.get(colorIndex);

            Map<String, Object> ev = new HashMap<>();
            ev.put("id", "shift-" + shift.getAvailabilityId());
            ev.put("start", start.toString());
            ev.put("end", end.toString());
            ev.put("display", "background");
            ev.put("backgroundColor", color);
            ev.put("borderColor", color);
            ev.put("extendedProps", Map.of(
            	    "stylistId", stylistId,
            	    "stylistName", stylistName,
            	    "type", "shift"
            	));


            events.add(ev);
            
            if (shift.getBreakTimes() != null) {
                for (var br : shift.getBreakTimes()) {

                    LocalDateTime bStart =
                        LocalDateTime.of(date, br.getBreakStartTime());
                    LocalDateTime bEnd =
                        LocalDateTime.of(date, br.getBreakEndTime());

                    Map<String, Object> brEv = new HashMap<>();
                    brEv.put("id", "break-" + br.getBreakId());
                    brEv.put("start", bStart.toString());
                    brEv.put("end", bEnd.toString());
                    brEv.put("display", "background");
                    brEv.put("extendedProps", Map.of(
                    	    "stylistId", stylistId,
                    	    "stylistName", stylistName,
                    	    "type", "break"
                    	));

                    
                    brEv.put("classNames", List.of("break"));


                    events.add(brEv);
                }
            }


        }
        
        

        return events;
    }



    @ResponseBody
    @GetMapping(value = "/calendar/events", produces = "application/json")
    public List<Map<String, Object>> getEvents() {

        List<Appointment> appts = appointmentService.getAllAppointments();
        Map<Long, Profile> profileCache = new HashMap<>();

        // List of distinct, soft colors
        List<String> colorPalette = List.of(
            "#FF7070", // light red
            "#FFB870", // light orange
            "#FFFF70", // light yellow
            "#85FF85", // light green
            "#70B8FF", // light blue
            "#B870FF"  // light purple
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
    
    @PostMapping("/calendar/update")
    @ResponseBody
    public Map<String, Object> updateEvent(@RequestBody CalendarEventDTO dto) {

        Map<String, Object> result = new HashMap<>();

        try {
            // Parse FullCalendar UTC timestamps
            Instant instantStart = Instant.parse(dto.getStart());
            Instant instantEnd = dto.getEnd() != null ? Instant.parse(dto.getEnd()) : null;

            ZoneId zone = ZoneId.systemDefault();

            LocalDateTime start = LocalDateTime.ofInstant(instantStart, zone);
            LocalDateTime end = instantEnd != null ? LocalDateTime.ofInstant(instantEnd, zone) : null;

            Appointment appt = appointmentService.getById(dto.getId());

            // Validate using your existing logic
            String validationError = appointmentService.validateAppointmentMove(appt, start, end);
            if (validationError != null) {
                result.put("status", "error");
                result.put("message", validationError);
                return result;
            }

            // Save new times
            appt.setScheduledStartDateTime(start);
            appt.setDurationMinutes((int) java.time.Duration.between(start, end).toMinutes());
            appointmentService.save(appt);

            result.put("status", "ok");
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("status", "error");
            result.put("message", "Server error: " + ex.getMessage());
            return result;
        }
    }




}
