package com.LilliputSalon.SalonApp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.AppointmentService;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.domain.UserType;
import com.LilliputSalon.SalonApp.domain.WalkIn;
import com.LilliputSalon.SalonApp.domain.WalkInRequestedService;
import com.LilliputSalon.SalonApp.dto.CreateWalkInDTO;
import com.LilliputSalon.SalonApp.dto.CreateWalkInServiceDTO;
import com.LilliputSalon.SalonApp.dto.NewCustomerDTO;
import com.LilliputSalon.SalonApp.dto.WalkInQueueDTO;
import com.LilliputSalon.SalonApp.repository.AppointmentRepository;
import com.LilliputSalon.SalonApp.repository.AppointmentServiceRepository;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.repository.UserTypeRepository;
import com.LilliputSalon.SalonApp.repository.WalkInRepository;
import com.LilliputSalon.SalonApp.repository.WalkInRequestedServiceRepository;


@Service
public class WalkInManagerService {

    private static final String STATUS_WAITING   = "WAITING";
    private static final String STATUS_IN_SERVICE = "IN_SERVICE";

    private static final int MAX_LOOKAHEAD_DAYS = 14;
    private static final int SLOT_INCREMENT_MINUTES = 15;

    private final WalkInRepository walkInRepo;
    private final WalkInRequestedServiceRepository walkInServiceRepo;
    private final ServiceRepository serviceRepo;
    private final AppointmentRepository appointmentRepo;
    private final AppointmentServiceRepository appointmentServiceRepo;
    private final AvailibilityRepository availabilityRepo;
    private final UserRepository userRepo;
    private final UserTypeRepository userTypeRepo;
    private final ProfileRepository profileRepo;
    

    public WalkInManagerService(
            WalkInRepository walkInRepo,
            WalkInRequestedServiceRepository walkInServiceRepo,
            ServiceRepository serviceRepo,
            AppointmentRepository appointmentRepo,
            AppointmentServiceRepository appointmentServiceRepo,
            AvailibilityRepository availabilityRepo,
            UserRepository userRepo,
            UserTypeRepository userTypeRepo,
            ProfileRepository profileRepo
    ) {
        this.walkInRepo = walkInRepo;
        this.walkInServiceRepo = walkInServiceRepo;
        this.serviceRepo = serviceRepo;
        this.appointmentRepo = appointmentRepo;
        this.appointmentServiceRepo = appointmentServiceRepo;
        this.availabilityRepo = availabilityRepo;
        this.userRepo = userRepo;
        this.userTypeRepo = userTypeRepo;
        this.profileRepo = profileRepo;
    }

    // --------------------------------------------------
    // CREATE WALK-IN
    // --------------------------------------------------
    @Transactional
    public WalkIn create(CreateWalkInDTO dto) {

        User customer;

        // -------------------------
        // 1️⃣ Resolve customer
        // -------------------------
        if (dto.getCustomerId() != null) {

            customer = userRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        } else if (dto.getNewCustomer() != null) {

            customer = createInactiveCustomer(dto.getNewCustomer());

        } else {
            throw new IllegalArgumentException("Customer information required");
        }

        // -------------------------
        // 2️⃣ Load services
        // -------------------------
        List<Long> serviceIds = dto.getServices()
            .stream()
            .map(CreateWalkInServiceDTO::getServiceId)
            .toList();

        List<com.LilliputSalon.SalonApp.domain.Service> services = serviceRepo.findAllById(serviceIds);

        if (services.isEmpty()) {
            throw new IllegalArgumentException("At least one service required");
        }

        int totalMinutes = services.stream()
            .mapToInt(s -> s.getTypicalDurationMinutes())
            .sum();

        // -------------------------
        // 3️⃣ Create walk-in
        // -------------------------
        WalkIn walkIn = new WalkIn();
        walkIn.setCustomerId(customer.getId());
        walkIn.setTimeEntered(LocalDateTime.now());
        walkIn.setEstimatedWaitMinutes(totalMinutes);
        walkIn.setStatus("WAITING");

        walkInRepo.save(walkIn);

        // -------------------------
        // 4️⃣ Attach services
        // -------------------------
        for (com.LilliputSalon.SalonApp.domain.Service s: services) {
            WalkInRequestedService wrs = new WalkInRequestedService();
            wrs.setWalkIn(walkIn);
            wrs.setService(s);
            wrs.setEstimatedDurationMinutes(s.getTypicalDurationMinutes());
            wrs.setEstimatedPrice(s.getBasePrice());
            walkInServiceRepo.save(wrs);
        }

        return walkIn;
    }

    
    @Transactional
    public User createInactiveCustomer(NewCustomerDTO dto) {

        // Generate a throwaway password (never used)
        String tempPassword = "TEMP_GUEST";

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(tempPassword);
        user.setIsActive(false); // 🚫 cannot log in

        userRepo.save(user);

        UserType customerType =
            userTypeRepo.findById((long) 3)
                .orElseThrow(() -> new RuntimeException("UserType CUSTOMER missing"));

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUserType(customerType);
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhone(dto.getPhone());

        profileRepo.save(profile);

        return user;
    }





    // --------------------------------------------------
    // QUEUE
    // --------------------------------------------------
    public List<WalkIn> getActiveQueue() {
        return walkInRepo.findByStatusOrderByTimeEnteredAsc(STATUS_WAITING);
    }
    
    public List<WalkInQueueDTO> getQueueDTOs() {

        return walkInRepo.findByStatusOrderByTimeEnteredAsc("WAITING")
            .stream()
            .map(w -> {

                WalkInQueueDTO dto = new WalkInQueueDTO();
                dto.setWalkInId(w.getWalkInId());
                dto.setCustomerId(w.getCustomerId());
                dto.setEstimatedWaitMinutes(w.getEstimatedWaitMinutes());

                Profile p = profileRepo.findByUser_Id(w.getCustomerId()).orElse(null);
                dto.setCustomerName(
                    p != null ? p.getFirstName() + " " + p.getLastName() : "Guest"
                );

                List<String> services =
                    walkInServiceRepo.findByWalkIn_WalkInId(w.getWalkInId())
                        .stream()
                        .map(ws -> ws.getService().getName())
                        .toList();

                dto.setServices(services);

                return dto;
            })
            .toList();
    }


    // --------------------------------------------------
    // STATUS
    // --------------------------------------------------
    @Transactional
    public void updateStatus(Integer walkInId, String status) {
        WalkIn walkIn = getWalkIn(walkInId);
        walkIn.setStatus(status);
        walkInRepo.save(walkIn);
    }

    // --------------------------------------------------
    // CONVERT TO APPOINTMENT
    // --------------------------------------------------
    @Transactional
    public Appointment convertToAppointment(Integer walkInId, Integer stylistId) {

        WalkIn walkIn = getWaitingWalkIn(walkInId);
        List<WalkInRequestedService> services =
                walkInServiceRepo.findByWalkIn_WalkInId(walkInId);

        if (services.isEmpty()) {
            throw new IllegalStateException("Walk-in has no services");
        }

        int totalMinutes = services.stream()
                .mapToInt(WalkInRequestedService::getEstimatedDurationMinutes)
                .sum();

        LocalDateTime start = findNextAvailableSlot(stylistId, totalMinutes);
        if (start == null) {
            throw new IllegalStateException("No available slot for stylist");
        }

        Appointment appt = createAppointment(walkIn, stylistId, start, totalMinutes);
        attachAppointmentServices(appt, services);

        walkIn.setAssignedStylistId(stylistId);
        walkIn.setStatus(STATUS_IN_SERVICE);
        walkInRepo.save(walkIn);

        return appt;
    }

    // --------------------------------------------------
    // SLOT SEARCH
    // --------------------------------------------------
    private LocalDateTime findNextAvailableSlot(Integer stylistId, int durationMinutes) {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(MAX_LOOKAHEAD_DAYS);

        for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {

            Availability availability =
                    availabilityRepo.findByUser_IdAndWorkDate(stylistId.longValue(), date);

            if (availability == null || !availability.getIsAvailable()) {
                continue;
            }

            LocalTime cursor = availability.getDayStartTime();
            LocalTime dayEnd = availability.getDayEndTime();

            while (!cursor.plusMinutes(durationMinutes).isAfter(dayEnd)) {

                LocalDateTime start = LocalDateTime.of(date, cursor);
                LocalDateTime end   = start.plusMinutes(durationMinutes);

                if (overlapsBreak(availability, start, end)) {
                    cursor = cursor.plusMinutes(SLOT_INCREMENT_MINUTES);
                    continue;
                }

                boolean overlap =
                        appointmentRepo.countOverlappingAppointments(
                                stylistId, start, end, null
                        ) > 0;

                if (!overlap) {
                    return start;
                }

                cursor = cursor.plusMinutes(SLOT_INCREMENT_MINUTES);
            }
        }

        return null;
    }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------
    private WalkIn getWalkIn(Integer id) {
        return walkInRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Walk-in not found"));
    }

    private WalkIn getWaitingWalkIn(Integer id) {
        WalkIn walkIn = getWalkIn(id);
        if (!STATUS_WAITING.equals(walkIn.getStatus())) {
            throw new IllegalStateException("Walk-in is not waiting");
        }
        return walkIn;
    }

    private List<com.LilliputSalon.SalonApp.domain.Service> loadServices(List<Long> serviceIds) {
        List<com.LilliputSalon.SalonApp.domain.Service> services =
                serviceRepo.findAllById(serviceIds);

        if (services.isEmpty()) {
            throw new IllegalArgumentException("At least one service is required");
        }
        return services;
    }


    private int calculateTotalMinutes(
            List<com.LilliputSalon.SalonApp.domain.Service> services
    ) {
        return services.stream()
                .mapToInt(com.LilliputSalon.SalonApp.domain.Service::getTypicalDurationMinutes)
                .sum();
    }


    private void saveRequestedServices(WalkIn walkIn, List<com.LilliputSalon.SalonApp.domain.Service> services) {
        for (com.LilliputSalon.SalonApp.domain.Service s : services) {
            WalkInRequestedService wrs = new WalkInRequestedService();
            wrs.setWalkIn(walkIn);
            wrs.setService(s);
            wrs.setEstimatedDurationMinutes(s.getTypicalDurationMinutes());
            wrs.setEstimatedPrice(s.getBasePrice());
            walkInServiceRepo.save(wrs);
        }
    }

    private Appointment createAppointment(
            WalkIn walkIn,
            Integer stylistId,
            LocalDateTime start,
            int durationMinutes
    ) {
        Appointment appt = new Appointment();
        appt.setCustomerId(walkIn.getCustomerId());
        appt.setStylistId(stylistId);
        appt.setScheduledStartDateTime(start);
        appt.setDurationMinutes(durationMinutes);
        appt.setStatus("Scheduled");
        appt.setIsCompleted(false);

        return appointmentRepo.save(appt);
    }

    private void attachAppointmentServices(
            Appointment appt,
            List<WalkInRequestedService> services
    ) {
        for (WalkInRequestedService w : services) {
            AppointmentService as = new AppointmentService();
            as.setAppointment(appt);
            as.setService(w.getService());
            as.setActualDurationMinutes(w.getEstimatedDurationMinutes());
            as.setActualPrice(w.getEstimatedPrice());
            appointmentServiceRepo.save(as);
        }
    }

    private boolean overlapsBreak(
            Availability availability,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return availability.getBreakTimes().stream()
                .anyMatch(b ->
                        start.toLocalTime().isBefore(b.getBreakEndTime()) &&
                        end.toLocalTime().isAfter(b.getBreakStartTime())
                );
    }
}
