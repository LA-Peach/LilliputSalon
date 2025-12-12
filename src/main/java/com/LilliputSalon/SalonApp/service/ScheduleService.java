package com.LilliputSalon.SalonApp.service;

import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BreakTime;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.dto.ScheduleEventDTO;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;
import com.LilliputSalon.SalonApp.repository.BreakTimeRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.BusinessHoursRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ScheduleService {

    private final AvailibilityRepository availabilityRepo;
    private final BreakTimeRepository breakRepo;
    private final ProfileRepository profileRepo;
    private final BusinessHoursRepository businessHoursRepo;

    public ScheduleService(
            AvailibilityRepository availabilityRepo,
            BreakTimeRepository breakRepo,
            ProfileRepository profileRepo,
            BusinessHoursRepository businessHoursRepo) {
        this.availabilityRepo = availabilityRepo;
        this.breakRepo = breakRepo;
        this.profileRepo = profileRepo;
        this.businessHoursRepo = businessHoursRepo;
    }

    /* --------------------------------------------
       BUSINESS HOURS
       -------------------------------------------- */
    public List<Map<String, Object>> getBusinessHours() {
        List<BusinessHours> list = businessHoursRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>(list.size());

        for (BusinessHours bh : list) {
            Map<String, Object> row = new HashMap<>();

            row.put("daysOfWeek", List.of(bh.getDayOfWeek()));

            boolean closed = Boolean.TRUE.equals(bh.getIsClosed())
                    || bh.getOpenTime() == null
                    || bh.getCloseTime() == null;

            if (closed) {
                row.put("startTime", "00:00");
                row.put("endTime", "00:00");
            } else {
                row.put("startTime", bh.getOpenTime().toString());
                row.put("endTime", bh.getCloseTime().toString());
            }

            result.add(row);
        }

        return result;
    }

    public List<Profile> getAllStylists() {
        return profileRepo.findByIsActiveStylistTrue();
    }


    /* --------------------------------------------
    CALENDAR EVENTS (ALL STYLISTS — SALON OVERVIEW)
    -------------------------------------------- */
	 public List<ScheduleEventDTO> getAllCalendarEvents() {
	
	     List<Availability> allAvailabilities = availabilityRepo.findAll();
	     List<ScheduleEventDTO> events = new ArrayList<>();
	
	     for (Availability availability : allAvailabilities) {
	
	         // Shift (availability)
	         events.add(toAvailabilityEvent(availability));
	
	         // Breaks inside that shift
	         List<BreakTime> breaks =
	                 breakRepo.findByAvailability_AvailabilityId(availability.getAvailabilityId());
	
	         for (BreakTime b : breaks) {
	             events.add(toBreakEvent(availability, b));
	         }
	     }
	
	     return events;
	 }


    /* --------------------------------------------
       CALENDAR EVENTS (for FullCalendar)
       -------------------------------------------- */
    public List<ScheduleEventDTO> getCalendarEventsForStylist(Long stylistId) {
        if (stylistId == null) {
            return List.of();
        }

        List<Availability> availabilities = availabilityRepo.findByUser_Id(stylistId);
        List<ScheduleEventDTO> events = new ArrayList<>();

        for (Availability availability : availabilities) {
            // Shift event
            events.add(toAvailabilityEvent(availability));

            // Break events attached to this shift
            List<BreakTime> breaks =
                    breakRepo.findByAvailability_AvailabilityId(availability.getAvailabilityId());

            for (BreakTime b : breaks) {
                events.add(toBreakEvent(availability, b));
            }
        }

        return events;
    }

    private ScheduleEventDTO toAvailabilityEvent(Availability availability) {
        LocalDateTime start = LocalDateTime.of(
                availability.getWorkDate(),
                availability.getDayStartTime()
        );
        LocalDateTime end = LocalDateTime.of(
                availability.getWorkDate(),
                availability.getDayEndTime()
        );

        Long stylistId = availability.getUser() != null ? availability.getUser().getId() : null;

        return new ScheduleEventDTO(
                availability.getAvailabilityId(),
                "Available",
                start.toString(),
                end.toString(),
                "availability",
                stylistId
        );
    }

    private ScheduleEventDTO toBreakEvent(Availability parent, BreakTime breakTime) {
        LocalDate workDate = parent.getWorkDate();
        LocalDateTime bStart = LocalDateTime.of(workDate, breakTime.getBreakStartTime());
        LocalDateTime bEnd = LocalDateTime.of(workDate, breakTime.getBreakEndTime());

        Long stylistId = parent.getUser() != null ? parent.getUser().getId() : null;

        String title = breakTime.getBreakType() != null
                ? breakTime.getBreakType()
                : "Break";

        return new ScheduleEventDTO(
                breakTime.getBreakId(),
                title,
                bStart.toString(),
                bEnd.toString(),
                "break",
                stylistId
        );
    }

    /* --------------------------------------------
       CREATE BLOCK (SHIFT / BREAK)
       -------------------------------------------- */
    public void createBlock(String type, String startISO, String endISO, Long stylistId) {

        LocalDateTime start = parseDateTime(startISO);
        LocalDateTime end = parseDateTime(endISO);

        if (!end.isAfter(start)) {
            throw new RuntimeException("End time must be after start time.");
        }

        Profile stylist = profileRepo.findByUser_Id(stylistId)
                .orElseThrow(() -> new RuntimeException("Stylist not found"));

        String normalizedType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);

        switch (normalizedType) {
            case "availability" -> createOrUpdateAvailability(stylist, start, end);
            case "break"        -> createBreak(stylist, start, end);
            default -> throw new RuntimeException("Unknown block type: " + type);
        }
    }

    private void createOrUpdateAvailability(Profile stylist, LocalDateTime start, LocalDateTime end) {
        LocalDate workDate = start.toLocalDate();

        Availability existing = availabilityRepo
                .findByUser_IdAndWorkDate(stylist.getUser().getId(), workDate);

        if (existing == null) {
            Availability availability = new Availability();
            availability.setUser(stylist.getUser());
            availability.setWorkDate(workDate);
            availability.setDayStartTime(start.toLocalTime());
            availability.setDayEndTime(end.toLocalTime());
            availability.setIsAvailable(true);
            availabilityRepo.save(availability);
        } else {
            existing.setDayStartTime(start.toLocalTime());
            existing.setDayEndTime(end.toLocalTime());
            availabilityRepo.save(existing);
        }
    }

    private void createBreak(Profile stylist, LocalDateTime start, LocalDateTime end) {
        LocalDate workDate = start.toLocalDate();

        Availability parent = availabilityRepo
                .findByUser_IdAndWorkDate(stylist.getUser().getId(), workDate);

        if (parent == null) {
            throw new RuntimeException("No shift (availability) exists for this stylist on this day.");
        }

        LocalTime breakStart = start.toLocalTime();
        LocalTime breakEnd = end.toLocalTime();

        // Must be inside shift
        if (breakStart.isBefore(parent.getDayStartTime()) ||
                breakEnd.isAfter(parent.getDayEndTime())) {
            throw new RuntimeException("Break must be within the stylist's shift hours.");
        }

        // Check overlap with existing breaks
        List<BreakTime> existingBreaks = parent.getBreakTimes();
        if (existingBreaks != null) {
            for (BreakTime existing : existingBreaks) {
                boolean overlaps =
                        !(breakEnd.isBefore(existing.getBreakStartTime()) ||
                                breakStart.isAfter(existing.getBreakEndTime()));
                if (overlaps) {
                    throw new RuntimeException("Break overlaps an existing break.");
                }
            }
        }

        BreakTime b = new BreakTime();
        b.setAvailability(parent);
        b.setBreakStartTime(breakStart);
        b.setBreakEndTime(breakEnd);
        b.setBreakType("Break");
        breakRepo.save(b);
    }

    /* --------------------------------------------
       UPDATE BLOCK (drag/drop or resize)
       -------------------------------------------- */
    public boolean updateBlock(Integer id, Long requestedStylistId, String startISO, String endISO) {

        LocalDateTime start = parseDateTime(startISO);
        LocalDateTime end = parseDateTime(endISO);

        if (!end.isAfter(start)) {
            throw new RuntimeException("End time must be after start time.");
        }

        // Try to update availability (shift)
        Availability availability = availabilityRepo.findById(id).orElse(null);
        if (availability != null) {
            return updateAvailability(availability, requestedStylistId, start, end);
        }

        // Try to update break
        BreakTime breakTime = breakRepo.findById(id).orElse(null);
        if (breakTime != null) {
            return updateBreak(breakTime, start, end);
        }

        // Not found
        return false;
    }

    private boolean updateAvailability(Availability availability,
                                       Long requestedStylistId,
                                       LocalDateTime start,
                                       LocalDateTime end) {

        Long ownerId = availability.getUser().getId();
        if (!ownerId.equals(requestedStylistId)) {
            // Security / consistency check
            throw new RuntimeException("Shift belongs to a different stylist.");
        }

        availability.setWorkDate(start.toLocalDate());
        availability.setDayStartTime(start.toLocalTime());
        availability.setDayEndTime(end.toLocalTime());
        availabilityRepo.save(availability);
        return true;
    }

    private boolean updateBreak(BreakTime breakTime,
                                LocalDateTime start,
                                LocalDateTime end) {

        LocalDate newDate = start.toLocalDate();
        Availability oldParent = breakTime.getAvailability();

        // Same shift date → just adjust times
        if (oldParent.getWorkDate().equals(newDate)) {
            breakTime.setBreakStartTime(start.toLocalTime());
            breakTime.setBreakEndTime(end.toLocalTime());
            breakRepo.save(breakTime);
            return true;
        }

        // Different date → move to another day's shift
        Availability newParent = availabilityRepo
                .findByUser_IdAndWorkDate(oldParent.getUser().getId(), newDate);

        if (newParent == null) {
            throw new RuntimeException("Cannot move break: no shift exists on this day.");
        }

        LocalTime newStart = start.toLocalTime();
        LocalTime newEnd = end.toLocalTime();

        // Must stay inside new shift
        if (newStart.isBefore(newParent.getDayStartTime()) ||
                newEnd.isAfter(newParent.getDayEndTime())) {
            throw new RuntimeException("Break must stay within the new day's shift hours.");
        }

        breakTime.setAvailability(newParent);
        breakTime.setBreakStartTime(newStart);
        breakTime.setBreakEndTime(newEnd);
        breakRepo.save(breakTime);
        return true;
    }

    /* --------------------------------------------
       DELETE BLOCK
       -------------------------------------------- */
    public boolean deleteBlock(Integer id) {

        if (availabilityRepo.existsById(id)) {
            availabilityRepo.deleteById(id);
            return true;
        }

        if (breakRepo.existsById(id)) {
            breakRepo.deleteById(id);
            return true;
        }

        return false;
    }

    /* --------------------------------------------
       HELPER: parse ISO local date-time
       -------------------------------------------- */
    private LocalDateTime parseDateTime(String iso) {
        if (iso == null) {
            throw new RuntimeException("Date/time value is required.");
        }
        return LocalDateTime.parse(iso);
    }
}
