package com.LilliputSalon.SalonApp.web;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.dto.CalendarEventDTO;
import com.LilliputSalon.SalonApp.dto.CreateAppointmentDTO;
import com.LilliputSalon.SalonApp.repository.BusinessHoursRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.repository.UserTypeRepository;
import com.LilliputSalon.SalonApp.service.AppointmentManagerService;

@Controller
@PreAuthorize("hasAnyRole('OWNER','STYLIST')")
public class CalendarController {

    private final AppointmentManagerService appointmentService;
    private final ProfileRepository profileRepo;
    private final BusinessHoursRepository businessHoursRepo;
    private final UserRepository userRepo;
    private final UserTypeRepository userTypeRepo;



    public CalendarController(AppointmentManagerService appointmentService,
                              ProfileRepository profileRepo,
                              BusinessHoursRepository businessHoursRepo,
                              UserRepository userRepo,
                              UserTypeRepository userTypeRepo) {
        this.appointmentService = appointmentService;
        this.profileRepo = profileRepo;
        this.businessHoursRepo = businessHoursRepo;
        this.userRepo = userRepo;
        this.userTypeRepo = userTypeRepo;
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
            var end = start.plusMinutes(appt.getTotalDurationMinutes());

            ev.put("start", start.toString());
            ev.put("end", end.toString());

            Profile stylist = profileCache.computeIfAbsent(
            	    appt.getStylistId().longValue(),
            	    id -> profileRepo.findByUser_Id(id).orElse(null)
            	);

            	Profile customer = profileCache.computeIfAbsent(
            	    appt.getCustomerId().longValue(),
            	    id -> profileRepo.findByUser_Id(id).orElse(null)
            	);

            String stylistName = stylist != null ? stylist.getFirstName() : "Stylist";
            String customerName = customer != null ? customer.getFirstName() : "Client";

            ev.put("title", stylistName + " — " + customerName);

            // 🎨 Assign stylist-specific color
            int colorIndex = Math.toIntExact(appt.getStylistId() % colorPalette.size());
            ev.put("backgroundColor", colorPalette.get(colorIndex));
            ev.put("borderColor", colorPalette.get(colorIndex));

            ev.put("extendedProps", Map.of(
            	    "status", appt.getStatus()
            	));


            events.add(ev);
        }

        return events;
    }

    @PostMapping("/calendar/update")
    @ResponseBody
    public Map<String, Object> updateEvent(@RequestBody CalendarEventDTO dto) {

        Map<String, Object> result = new HashMap<>();

        try {
            Instant instantStart = Instant.parse(dto.getStart());
            Instant instantEnd = dto.getEnd() != null
                    ? Instant.parse(dto.getEnd())
                    : null;

            ZoneId zone = ZoneId.systemDefault();

            LocalDateTime start = LocalDateTime.ofInstant(instantStart, zone);
            LocalDateTime end = instantEnd != null
                    ? LocalDateTime.ofInstant(instantEnd, zone)
                    : null;

            Appointment appt = appointmentService.getById(dto.getId());

            String validationError =
                    appointmentService.validateAppointmentMove(appt, start, end);
            if (validationError != null) {
                result.put("status", "error");
                result.put("message", validationError);
                return result;
            }

            appt.setScheduledStartDateTime(start);
            appointmentService.save(appt);

            result.put("status", "ok");
            return result;

        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("status", "error");
            result.put("message", ex.getMessage());
            return result;
        }
    }


    @PostMapping("/appointments/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('OWNER','STYLIST')")
    public ResponseEntity<?> deleteAppointment(@PathVariable Integer id) {
        try {
            appointmentService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500)
                .body("Unable to delete appointment");
        }
    }

    @PostMapping("/appointments/create")
    @ResponseBody
    public Map<String, Object> createAppointment(@RequestBody CreateAppointmentDTO dto) {

        Map<String, Object> result = new HashMap<>();

        try {
            Long customerId = resolveOrCreateCustomer(dto);

            BusinessHours bh = businessHoursRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Business hours missing"));

            appointmentService.create(dto, customerId, bh);

            result.put("status", "ok");
            return result;

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            return result;
        }
    }


    private Long resolveOrCreateCustomer(CreateAppointmentDTO dto) {

        String email = dto.getCustomerEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Customer email is required.");
        }

        var matches = profileRepo.searchByEmail(email.trim());

        if (!matches.isEmpty()) {
            return matches.get(0).getUser().getId();
        }


        // create walk-in guest
        User guestUser = new User();
        guestUser.setEmail(email.trim());
        guestUser.setIsActive(false);
        guestUser.setPasswordHash("TEMP_GUEST");
        userRepo.save(guestUser);

        Profile guestProfile = new Profile();
        guestProfile.setUser(guestUser);
        guestProfile.setFirstName(
            Optional.ofNullable(dto.getGuestFirstName()).filter(s -> !s.isBlank()).orElse("Walk-in")
        );
        guestProfile.setLastName(
            Optional.ofNullable(dto.getGuestLastName()).orElse("")
        );
        guestProfile.setPhone(dto.getGuestPhone());
        guestProfile.setIsActiveStylist(false);
        guestProfile.setUserType(
            userTypeRepo.findByTypeNameIgnoreCase("CUSTOMER")
                .orElseThrow()
        );

        profileRepo.save(guestProfile);

        return guestUser.getId();
    }




}
