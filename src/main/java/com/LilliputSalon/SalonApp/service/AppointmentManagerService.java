package com.LilliputSalon.SalonApp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.AppointmentService;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BreakTime;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.dto.CreateAppointmentDTO;
import com.LilliputSalon.SalonApp.dto.WaitTimeDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentRepository;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository.TopServiceCount;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;
import com.LilliputSalon.SalonApp.repository.BusinessHoursRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.security.AppointmentAvailabilityException;
import com.LilliputSalon.SalonApp.security.AppointmentOverlapException;

import jakarta.transaction.Transactional;

@Service
public class AppointmentManagerService {

    private final AppointmentRepository repo;
    private final AvailibilityRepository availabilityRepo;
    private final AppointmentServiceRepository ASrepo;
    private final ServiceRepository serviceRepo;
    private final BusinessHoursRepository businessHoursRepo;
    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;

    public AppointmentManagerService(
            AppointmentRepository repo,
            AvailibilityRepository availabilityRepo,
            AppointmentServiceRepository ASrepo,
            ServiceRepository serviceRepo,
            BusinessHoursRepository businessHoursRepo,
            UserRepository userRepo,
            ProfileRepository profileRepo
    ) {
        this.repo = repo;
        this.availabilityRepo = availabilityRepo;
        this.ASrepo = ASrepo;
        this.serviceRepo = serviceRepo;
        this.businessHoursRepo = businessHoursRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }



    public Appointment getById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public List<Appointment> getByCustomer(Long id) {
        return repo.findByCustomerId(id);
    }


    public List<Appointment> getTodayForStylist(Long stylistId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusHours(23).plusMinutes(59).plusSeconds(59);

        return repo.findByStylistIdAndScheduledStartDateTimeBetween(
                stylistId,
                start,
                end
        );
    }

    public List<Appointment> getTodayOrNextForStylist(Long stylistId) {

        LocalDate today = LocalDate.now();

        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.atTime(23, 59, 59);

        // 1) Try to load today's appointments
        List<Appointment> todays = repo.findByStylistIdAndScheduledStartDateTimeBetween(
                stylistId, startToday, endToday
        );

        if (!todays.isEmpty()) {
            return todays; // Success: show today's appointments
        }

        // 2) None today → find the next upcoming appointment
        var nextApptOpt = repo.findFirstByStylistIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
                stylistId,
                endToday
        );

        if (nextApptOpt.isEmpty()) {
            // No future appointments at all
            return List.of();
        }

        // 3) Load the full day of that next appointment
        LocalDate nextDate = nextApptOpt.get().getScheduledStartDateTime().toLocalDate();

        LocalDateTime startNext = nextDate.atStartOfDay();
        LocalDateTime endNext = nextDate.atTime(23, 59, 59);

        return repo.findByStylistIdAndScheduledStartDateTimeBetween(
                stylistId, startNext, endNext
        );
    }

    public Appointment getNextAppointmentForStylist(Long stylistId) {
        LocalDateTime now = LocalDateTime.now();
        return repo.findFirstByStylistIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
                stylistId, now
        ).orElse(null);
    }

    public Appointment getNextAppointmentForCustomer(Long customerId) {
        LocalDateTime now = LocalDateTime.now();
        return repo.findFirstByCustomerIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
                customerId, now
        ).orElse(null);
    }


    public List<Appointment> getByDate(LocalDateTime date) {
        return repo.findByScheduledStartDateTimeBetween(
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );
    }

    @Transactional
    public void markAppointmentComplete(Integer appointmentId) {

    	Appointment appt = repo.findById(appointmentId)
    	        .orElseThrow(() -> new RuntimeException("Appointment not found"));

    	    appt.setStatus("Completed");
    	    appt.setIsCompleted(true);

    	    User user = userRepo.findById(appt.getCustomerId())
    	        .orElse(null);

    	    // 🎯 Points logic
    	    if (user != null && Boolean.TRUE.equals(user.getIsActive())) {

    	        int points = calculatePoints(appt.getTotalAmount());
    	        appt.setPointsEarned(points);

    	    } else {
    	        appt.setPointsEarned(0);
    	    }

    	    repo.save(appt);


    }


    public List<Appointment> getAllAppointments() {
        return repo.findAllWithServices();
    }

    public String validateAppointmentMove(Appointment appt, LocalDateTime newStart, LocalDateTime newEnd) {

        Long stylistId = appt.getStylistId();
        LocalDate date = newStart.toLocalDate();

        // ------------------------
        // 1) Load stylist availability for date
        // ------------------------
        Availability availability = availabilityRepo
                .findByUser_IdAndWorkDate(Long.valueOf(stylistId), date);

        if (availability == null || !availability.getIsAvailable()) {
            return "Stylist is not available on this day.";
        }

        LocalTime start = newStart.toLocalTime();
        LocalTime end = newEnd.toLocalTime();

        // ------------------------
        // 2) Validate within working hours
        // ------------------------
        if (start.isBefore(availability.getDayStartTime()) ||
            end.isAfter(availability.getDayEndTime())) {
            return "Appointment is outside stylist's working hours.";
        }

	     // ------------------------
	     // 3) Validate breaks (ONLY overlap blocks)
	     // ------------------------
	     for (BreakTime b : availability.getBreakTimes()) {

	         LocalTime breakStart = b.getBreakStartTime();
	         LocalTime breakEnd   = b.getBreakEndTime();

	         if (start.isBefore(breakEnd) && end.isAfter(breakStart)) {
	             return "Appointment overlaps stylist break time.";
	         }
	     }


        // ------------------------
        // 4) Check for overlapping appointments
        // ------------------------
        List<Appointment> sameDay = repo.findByStylistIdAndScheduledStartDateTimeBetween(
                stylistId,
                date.atStartOfDay(),
                date.atTime(23, 59)
        );

        for (Appointment other : sameDay) {

            // Skip itself
            if (other.getAppointmentId().equals(appt.getAppointmentId())) {
				continue;
			}

            LocalDateTime oStart = other.getScheduledStartDateTime();
            LocalDateTime oEnd = oStart.plusMinutes(other.getTotalDurationMinutes());

            boolean overlap = newStart.isBefore(oEnd) && newEnd.isAfter(oStart);


            if (overlap) {
                return "Appointment overlaps another appointment.";
            }
        }

        return null; // VALID
    }



    public Appointment save(Appointment a) {
        return repo.save(a);
    }

    @Transactional
    public void delete(Integer appointmentId) {

        Appointment appt = repo.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appt.getIsCompleted()) {
            throw new IllegalStateException("Completed appointments cannot be deleted");
        }

        ASrepo.deleteByAppointmentId(appointmentId);
        repo.deleteById(appointmentId);
    }

    @Transactional
    public Appointment create(CreateAppointmentDTO dto, Long customerId, BusinessHours bh) {

        Instant instant = Instant.parse(dto.getStart());
        LocalDateTime start =
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault());   

        List<com.LilliputSalon.SalonApp.domain.Service> services =
            serviceRepo.findAllById(dto.getServiceIds());

        if (services.isEmpty()) {
            throw new RuntimeException("At least one service is required.");
        }

        int totalMinutes = services.stream()
            .mapToInt(s -> s.getTypicalDurationMinutes())
            .sum();

        LocalDateTime end = start.plusMinutes(totalMinutes);

        // 🔒 NEW: availability + working hours
        validateNewAppointment(dto.getStylistId(), start, end);

        // 🔒 Overlap check (already correct)
        boolean overlap =
            repo.countOverlappingAppointments(
                dto.getStylistId(), start, end, null
            ) > 0;

        if (overlap) {
            throw new AppointmentOverlapException(
                "This appointment overlaps an existing appointment."
            );
        }

        // Save appointment
        Appointment appt = new Appointment();
        appt.setCustomerId(customerId);
        appt.setStylistId(dto.getStylistId());
        appt.setScheduledStartDateTime(start);
        appt.setDurationMinutes(totalMinutes);
        appt.setStatus("Scheduled");
        appt.setIsCompleted(false);

        repo.save(appt);

        for (com.LilliputSalon.SalonApp.domain.Service s : services) {
            AppointmentService as = new AppointmentService();
            as.setAppointment(appt);
            as.setService(s);
            as.setActualPrice(s.getBasePrice());
            as.setActualDurationMinutes(s.getTypicalDurationMinutes());
            ASrepo.save(as);
        }

        return appt;
    }




	public List<Availability> getAllStylistShifts() {
		return availabilityRepo.findAllWithBreaks();
	}


	@Transactional
	public void updateAppointment(Integer id, LocalDateTime newStart) {

	    Appointment appt = repo.findById(id).orElseThrow();

	    LocalDateTime newEnd =
	        newStart.plusMinutes(appt.getDurationMinutes());

	    // 🔒 Availability + hours + breaks
	    validateNewAppointment(
	        appt.getStylistId(),
	        newStart,
	        newEnd
	    );

	    // 🔒 Overlap check (exclude self)
	    boolean overlap =
	        repo.countOverlappingAppointments(
	            appt.getStylistId(),
	            newStart,
	            newEnd,
	            appt.getAppointmentId()
	        ) > 0;

	    if (overlap) {
	        throw new AppointmentOverlapException(
	            "Appointment overlaps another appointment."
	        );
	    }

	    appt.setScheduledStartDateTime(newStart);
	    repo.save(appt);
	}



	public void updateServices(Appointment appt, List<Long> serviceIds) {
		// TODO Auto-generated method stub

	}

	private void validateNewAppointment(
		    Long stylistId,
		    LocalDateTime start,
		    LocalDateTime end
		) {
		    LocalDate date = start.toLocalDate();

		    Availability availability =
		        availabilityRepo.findByUser_IdAndWorkDate(
		            stylistId, date
		        );

		    if (availability == null) {
		        throw new AppointmentAvailabilityException(
		            "Stylist is not working on this date."
		        );
		    }

		    if (!Boolean.TRUE.equals(availability.getIsAvailable())) {
		        throw new AppointmentAvailabilityException(
		            "Stylist is marked unavailable on this date."
		        );
		    }

		    LocalTime startTime = start.toLocalTime();
		    LocalTime endTime   = end.toLocalTime();

		    if (startTime.isBefore(availability.getDayStartTime())
		        || endTime.isAfter(availability.getDayEndTime())) {

		        throw new AppointmentAvailabilityException(
		            "Appointment is outside the stylist's working hours."
		        );
		    }

		    for (BreakTime b : availability.getBreakTimes()) {
		        if (startTime.isBefore(b.getBreakEndTime())
		            && endTime.isAfter(b.getBreakStartTime())) {

		            throw new AppointmentAvailabilityException(
		                "Appointment overlaps the stylist's break time."
		            );
		        }
		    }
		}



	private int calculatePoints(BigDecimal totalAmount) {
	    if (totalAmount == null) {
			return 0;
		}

	    return totalAmount
	        .divide(BigDecimal.valueOf(10), 0, RoundingMode.FLOOR)
	        .intValue();
	}

	public record StylistDayView(
	        Profile stylist,
	        Availability availability,
	        List<Appointment> appointments
	) {}

	public List<StylistDayView> getOwnerDayView() {

	    LocalDate today = LocalDate.now();

	    // 1️⃣ Find the first date >= today with working stylists
	    LocalDate displayDate = availabilityRepo
	            .findFirstFutureWorkDate(today)
	            .orElse(today);

	    // 2️⃣ Load ONLY stylists working that date
	    List<Availability> availabilities =
	            availabilityRepo.findByWorkDateAndIsAvailableTrue(displayDate);

	    List<StylistDayView> result = new ArrayList<>();

	    for (Availability availability : availabilities) {

	        Profile stylistProfile =
	                profileRepo.findByUser_Id(availability.getUser().getId())
	                        .orElseThrow();

	        // 3️⃣ Load appointments for that stylist on that day
	        List<Appointment> appts =
	                repo.findByStylistIdAndScheduledStartDateTimeBetween(
	                        availability.getUser().getId(),
	                        displayDate.atStartOfDay(),
	                        displayDate.atTime(23, 59, 59)
	                );

	        result.add(new StylistDayView(
	                stylistProfile,
	                availability,
	                appts
	        ));
	    }

	    // Optional: sort by shift start time
	    result.sort((a, b) ->
	            a.availability().getDayStartTime()
	                    .compareTo(b.availability().getDayStartTime())
	    );

	    return result;
	}

	public List<Appointment> getAppointmentsForStylistOnDate(
	        Long stylistUserId,
	        LocalDate date
	) {
	    LocalDateTime start = date.atStartOfDay();
	    LocalDateTime end   = date.atTime(23, 59, 59);

	    return repo.findByStylistIdAndScheduledStartDateTimeBetween(
	            stylistUserId,
	            start,
	            end
	    );
	}

	public Availability getAvailabilityForUserOnDate(Long userId, LocalDate date) {
	    return availabilityRepo.findByUser_IdAndWorkDate(userId, date);
	}

	public Long getCompletedAppointmentCountForStylist(Long stylistProfileId) {
	    return repo.countByStylistIdAndIsCompletedTrue(stylistProfileId);
	}

	public List<TopServiceCount> getTopServicesForStylist(Long stylistProfileId) {
	    return ASrepo
	            .findTopServicesForStylist(stylistProfileId)
	            .stream()
	            .limit(3)
	            .toList();
	}

	public long getAppointmentsTodayCount() {

	    LocalDate today = LocalDate.now();

	    LocalDateTime start = today.atStartOfDay();
	    LocalDateTime end   = today.atTime(23, 59, 59);

	    return repo.countAppointmentsToday(start, end);
	}


	@Transactional
	public void cancelAppointment(Integer appointmentId, Long customerUserId) {

	    Appointment appt = repo.findById(appointmentId)
	            .orElseThrow(() -> new RuntimeException("Appointment not found"));

	    // 🔒 Ownership check
	    if (!appt.getCustomerId().equals(customerUserId)) {
	        throw new SecurityException("You are not allowed to cancel this appointment.");
	    }

	    // ❌ Already completed
	    if (Boolean.TRUE.equals(appt.getIsCompleted())) {
	        throw new IllegalStateException("Completed appointments cannot be cancelled.");
	    }

	    LocalDateTime now = LocalDateTime.now();

	    // ❌ Past appointment
	    if (appt.getScheduledStartDateTime().isBefore(now)) {
	        throw new IllegalStateException("Past appointments cannot be cancelled.");
	    }

	    // ❌ Within 12 hours
	    if (appt.getScheduledStartDateTime().isBefore(now.plusHours(12))) {
	        throw new IllegalStateException(
	                "Appointments within 12 hours cannot be cancelled."
	        );
	    }

	    // ✅ Safe to cancel
	    ASrepo.deleteByAppointmentId(appointmentId);
	    repo.delete(appt);
	}

	public List<LocalTime> getAvailableTimeSlots(
	        LocalDate date,
	        Long stylistId,
	        List<Long> serviceIds
	) {
	    int totalMinutes = serviceRepo.findAllById(serviceIds)
	            .stream()
	            .mapToInt(s -> s.getTypicalDurationMinutes())
	            .sum();

	    if (totalMinutes == 0) {
	        return List.of();
	    }

	    List<Availability> availabilities = new ArrayList<>();

	    if (stylistId == null) {
	        // No stylist preference → all working stylists
	        availabilities = availabilityRepo
	                .findByWorkDateAndIsAvailableTrue(date);
	    } else {
	        // Specific stylist
	        Availability a =
	                availabilityRepo.findByUser_IdAndWorkDate(stylistId, date);

	        if (a == null || !Boolean.TRUE.equals(a.getIsAvailable())) {
	            return List.of(); // stylist not working → no slots
	        }

	        availabilities.add(a);
	    }

	    List<LocalTime> slots = new ArrayList<>();

	    for (Availability a : availabilities) {

	        LocalTime cursor = a.getDayStartTime();
	        LocalTime end    = a.getDayEndTime();

	        while (!cursor.plusMinutes(totalMinutes).isAfter(end)) {

	            LocalTime slotStart = cursor;
	            LocalTime slotEnd   = cursor.plusMinutes(totalMinutes);

	            LocalDateTime start  = date.atTime(slotStart);
	            LocalDateTime finish = start.plusMinutes(totalMinutes);

	            boolean overlaps =
	                    repo.countOverlappingAppointments(
	                            a.getUser().getId(),
	                            start,
	                            finish,
	                            null
	                    ) > 0;

	            boolean breakOverlap =
	                    a.getBreakTimes().stream().anyMatch(b ->
	                            slotStart.isBefore(b.getBreakEndTime()) &&
	                            slotEnd.isAfter(b.getBreakStartTime())
	                    );

	            if (!overlaps && !breakOverlap) {
	                slots.add(slotStart);
	            }

	            cursor = cursor.plusMinutes(15);
	        }
	    }

	    return slots.stream().distinct().sorted().toList();
	}
	
	public Long findAvailableStylistForSlot(
	        LocalDate date,
	        LocalTime startTime,
	        List<Long> serviceIds
	) {
	    int totalMinutes = serviceRepo.findAllById(serviceIds)
	            .stream()
	            .mapToInt(s -> s.getTypicalDurationMinutes())
	            .sum();

	    if (totalMinutes <= 0) return null;

	    LocalDateTime start = date.atTime(startTime);
	    LocalDateTime end   = start.plusMinutes(totalMinutes);

	    // All working stylists that day
	    List<Availability> availabilities =
	            availabilityRepo.findByWorkDateAndIsAvailableTrue(date);

	    for (Availability a : availabilities) {
	        // within hours
	        if (startTime.isBefore(a.getDayStartTime()) ||
	            startTime.plusMinutes(totalMinutes).isAfter(a.getDayEndTime())) {
	            continue;
	        }

	        // not on break
	        boolean breakOverlap = a.getBreakTimes().stream().anyMatch(b ->
	                startTime.isBefore(b.getBreakEndTime()) &&
	                startTime.plusMinutes(totalMinutes).isAfter(b.getBreakStartTime())
	        );
	        if (breakOverlap) continue;

	        // not overlapping appointments
	        boolean overlaps =
	                repo.countOverlappingAppointments(
	                        a.getUser().getId(),
	                        start,
	                        end,
	                        null
	                ) > 0;

	        if (!overlaps) {
	            return a.getUser().getId(); // ✅ first available stylist
	        }
	    }

	    return null; // none available
	}
	
	public WaitTimeDTO calculateWalkInWaitTime() {

	    LocalDate today = LocalDate.now();
	    LocalDateTime now = LocalDateTime.now();

	    List<Availability> availabilities =
	            availabilityRepo.findByWorkDateAndIsAvailableTrue(today);

	    LocalDateTime bestTime = null;
	    Profile bestStylist = null;

	    for (Availability a : availabilities) {

	        LocalDateTime cursor = LocalDateTime.of(today, a.getDayStartTime());

	        if (cursor.isBefore(now)) {
	            cursor = now;
	        }

	        // 2️⃣ Load today's appointments sorted
	        List<Appointment> appts =
	                repo.findByStylistIdAndScheduledStartDateTimeBetween(
	                        a.getUser().getId(),
	                        today.atStartOfDay(),
	                        today.atTime(23, 59, 59)
	                ).stream()
	                 .sorted((x, y) ->
	                        x.getScheduledStartDateTime()
	                         .compareTo(y.getScheduledStartDateTime()))
	                 .toList();

	        // 3️⃣ Walk appointments
	        for (Appointment appt : appts) {

	            LocalDateTime apptStart = appt.getScheduledStartDateTime();
	            LocalDateTime apptEnd =
	                    apptStart.plusMinutes(appt.getDurationMinutes());

	            // If cursor overlaps appointment → jump to end
	            if (!cursor.isAfter(apptEnd) && cursor.isBefore(apptEnd)) {
	                if (cursor.isBefore(apptStart)) {
	                    break; // free before this appointment
	                }
	                cursor = apptEnd;
	            }
	        }

	        // 4️⃣ Skip breaks
	        for (BreakTime b : a.getBreakTimes()) {
	            LocalDateTime breakStart = LocalDateTime.of(today, b.getBreakStartTime());
	            LocalDateTime breakEnd   = LocalDateTime.of(today, b.getBreakEndTime());

	            if (!cursor.isBefore(breakStart) && cursor.isBefore(breakEnd)) {
	                cursor = breakEnd;
	            }
	        }

	        // 5️⃣ Must still be within shift
	        if (cursor.toLocalTime().isAfter(a.getDayEndTime())) {
	            continue;
	        }

	        // 6️⃣ Pick earliest free stylist
	        if (bestTime == null || cursor.isBefore(bestTime)) {
	            bestTime = cursor;
	            bestStylist =
	                    profileRepo.findByUser_Id(a.getUser().getId()).orElse(null);
	        }
	    }

	    if (bestTime == null || bestStylist == null) {
	        return new WaitTimeDTO(0, null, null);
	    }

	    int minutes =
	            Math.max(0, (int) java.time.Duration.between(now, bestTime).toMinutes());

	    return new WaitTimeDTO(
	            minutes,
	            bestStylist.getFirstName(),
	            bestTime.toLocalTime()
	    );
	}


}
