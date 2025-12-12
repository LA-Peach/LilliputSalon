package com.LilliputSalon.SalonApp.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BreakTime;
import com.LilliputSalon.SalonApp.dto.CalendarEventDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentRepository;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;

import jakarta.transaction.Transactional;

@Service
public class AppointmentManagerService {

    private final AppointmentRepository repo;
    private final AvailibilityRepository availabilityRepo;
    private final AppointmentServiceRepository ASrepo;

    public AppointmentManagerService(AppointmentRepository repo,
                                     AvailibilityRepository availabilityRepo,
                                     AppointmentServiceRepository ASrepo) {
        this.repo = repo;
        this.availabilityRepo = availabilityRepo;
        this.ASrepo = ASrepo;
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
        return repo.findAll();
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
            LocalDateTime oEnd = oStart.plusMinutes(other.getDurationMinutes());

            boolean overlap =
                    !(newEnd.isBefore(oStart) || newStart.isAfter(oEnd));

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
    public void create(CalendarEventDTO dto) {

        ZoneId zone = ZoneId.systemDefault();

        LocalDateTime start = LocalDateTime.ofInstant(
            Instant.parse(dto.getStart()), zone);

        LocalDateTime end = LocalDateTime.ofInstant(
            Instant.parse(dto.getEnd()), zone);

        Appointment appt = new Appointment();
        appt.setScheduledStartDateTime(start);
        appt.setDurationMinutes(
            (int) Duration.between(start, end).toMinutes()
        );

        // 🔑 stylist assignment
        appt.setStylistId(dto.getStylistId());

        appt.setStatus("Scheduled");
        appt.setIsCompleted(false);

        repo.save(appt);
    }





	public List<Availability> getAllStylistShifts() {
		return availabilityRepo.findAll();
	}
}
