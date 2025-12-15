package com.LilliputSalon.SalonApp.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.AppointmentService;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BreakTime;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.WalkIn;
import com.LilliputSalon.SalonApp.dto.CreateAppointmentDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentRepository;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;
import com.LilliputSalon.SalonApp.repository.BusinessHoursRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;
import com.LilliputSalon.SalonApp.repository.WalkInRepository;
import com.LilliputSalon.SalonApp.repository.WalkInRequestedServiceRepository;
import com.LilliputSalon.SalonApp.security.AppointmentAvailabilityException;
import com.LilliputSalon.SalonApp.security.AppointmentOverlapException;

import jakarta.transaction.Transactional;

@Service
public class AppointmentManagerService {

    private final AppointmentRepository repo;
    private final AvailibilityRepository availabilityRepo;
    private final AppointmentServiceRepository ASrepo;
    private final ServiceRepository serviceRepo;
    private final WalkInRepository walkInRepo;
    private final WalkInRequestedServiceRepository walkInServiceRepo;
    private final BusinessHoursRepository businessHoursRepo;

    public AppointmentManagerService(
            AppointmentRepository repo,
            AvailibilityRepository availabilityRepo,
            AppointmentServiceRepository ASrepo,
            ServiceRepository serviceRepo,
            WalkInRepository walkInRepo,
            WalkInRequestedServiceRepository walkInServiceRepo,
            BusinessHoursRepository businessHoursRepo
    ) {
        this.repo = repo;
        this.availabilityRepo = availabilityRepo;
        this.ASrepo = ASrepo;
        this.serviceRepo = serviceRepo;
        this.walkInRepo = walkInRepo;
        this.walkInServiceRepo = walkInServiceRepo;
        this.businessHoursRepo = businessHoursRepo;
    }



    public Appointment getById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public List<Appointment> getByCustomer(Integer id) {
        return repo.findByCustomerId(id);
    }


    public List<Appointment> getTodayForStylist(Integer stylistId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusHours(23).plusMinutes(59).plusSeconds(59);

        return repo.findByStylistIdAndScheduledStartDateTimeBetween(
                stylistId,
                start,
                end
        );
    }

    public List<Appointment> getTodayOrNextForStylist(Integer stylistId) {

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

    public Appointment getNextAppointmentForStylist(Integer stylistId) {
        LocalDateTime now = LocalDateTime.now();
        return repo.findFirstByStylistIdAndScheduledStartDateTimeAfterOrderByScheduledStartDateTimeAsc(
                stylistId, now
        ).orElse(null);
    }

    public Appointment getNextAppointmentForCustomer(Integer customerId) {
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

    public void markAppointmentComplete(Integer appointmentId) {

        Appointment appt = repo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Mark completed
        appt.setStatus("Completed");
        appt.setIsCompleted(true);

        // Award points = total amount (simple rule for now)
        if (appt.getTotalAmount() != null) {
            appt.setPointsEarned(appt.getTotalAmount().intValue());
        } else {
            appt.setPointsEarned(0);
        }

        repo.save(appt);
    }

    public List<Appointment> getAllAppointments() {
        return repo.findAllWithServices();
    }

    public String validateAppointmentMove(Appointment appt, LocalDateTime newStart, LocalDateTime newEnd) {

        int stylistId = appt.getStylistId();
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
		    Integer stylistId,
		    LocalDateTime start,
		    LocalDateTime end
		) {
		    LocalDate date = start.toLocalDate();

		    Availability availability =
		        availabilityRepo.findByUser_IdAndWorkDate(
		            stylistId.longValue(), date
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

	@Transactional
	public Appointment convertWalkInToAppointment(
	    Integer walkInId,
	    Integer stylistId,
	    LocalDateTime start
	) {
	    WalkIn wi = walkInRepo.findWithServices(walkInId); // from repo query
	    if (wi == null) {
			throw new RuntimeException("Walk-in not found");
		}

	    // Pull service IDs from requested services
	    // (adjust field names if yours differ)
	    List<Long> serviceIds = walkInServiceRepo.findByWalkIn_WalkInId(walkInId).stream()
	    	    .map(wrs -> wrs.getService().getId())
	    	    .toList();


	    if (serviceIds.isEmpty()) {
	        throw new RuntimeException("Walk-in has no requested services");
	    }

	    CreateAppointmentDTO dto = new CreateAppointmentDTO();
	    dto.setStylistId(stylistId);

	    ZoneId zone = ZoneId.systemDefault();
	    dto.setStart(start.atZone(zone).toInstant().toString());

	    dto.setServiceIds(serviceIds);

	    BusinessHours bh = businessHoursRepo.findById(1)
	        .orElseThrow(() -> new RuntimeException("Business hours missing"));

	    // Uses your existing availability + overlap enforcement inside create()
	    Appointment appt = create(dto, wi.getCustomerId(), bh);

	    wi.setStatus("CONVERTED");
	    wi.setAssignedStylistId(stylistId);
	    walkInRepo.save(wi);

	    return appt;
	}







}
