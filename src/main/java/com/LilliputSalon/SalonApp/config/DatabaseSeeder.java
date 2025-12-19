package com.LilliputSalon.SalonApp.config;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.Availability;
import com.LilliputSalon.SalonApp.domain.BreakTime;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.Profile;
import com.LilliputSalon.SalonApp.domain.ServiceCategory;
import com.LilliputSalon.SalonApp.domain.Services;
import com.LilliputSalon.SalonApp.domain.User;
import com.LilliputSalon.SalonApp.domain.UserType;
import com.LilliputSalon.SalonApp.repository.AppointmentRepository;
import com.LilliputSalon.SalonApp.repository.AvailibilityRepository;
import com.LilliputSalon.SalonApp.repository.BreakTimeRepository;
import com.LilliputSalon.SalonApp.repository.BusinessHoursRepository;
import com.LilliputSalon.SalonApp.repository.ProfileRepository;
import com.LilliputSalon.SalonApp.repository.ServiceCategoryRepository;
import com.LilliputSalon.SalonApp.repository.ServiceRepository;
import com.LilliputSalon.SalonApp.repository.UserRepository;
import com.LilliputSalon.SalonApp.repository.UserTypeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserTypeRepository userTypeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceRepository servicesRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final ProfileRepository profileRepository;
    private final AvailibilityRepository availabilityRepository;
    private final BreakTimeRepository breakTimeRepository;
    private final AppointmentRepository appointmentRepository;




    @Bean
    CommandLineRunner seedDatabase() {
        return args -> {
            seedUserTypes();
            seedServiceCategories();
            seedBusinessHours();
            seedOwnerWithProfile();
            seedStylists();
            seedServices();
            seedAvailability();
        };
    }

    @Transactional
    public void seedUserTypes() {
        if (userTypeRepository.count() == 0) {
            UserType owner = new UserType();
            owner.setTypeName("OWNER");

            UserType stylist = new UserType();
            stylist.setTypeName("STYLIST");

            UserType customer = new UserType();
            customer.setTypeName("CUSTOMER");

            userTypeRepository.saveAll(List.of(owner, stylist, customer));
        }
    }

    @Transactional
    public void seedOwnerWithProfile() {
        if (userRepository.findByEmail("owner@lilliputsalon.com").isPresent()) {
            return;
        }

        User owner = new User();
        owner.setEmail("owner@lilliputsalon.com");
        owner.setPasswordHash(passwordEncoder.encode("OwnerPassword!321"));
        owner.setIsActive(true);

        userRepository.save(owner);

        UserType ownerType = userTypeRepository
                .findByTypeNameIgnoreCase("OWNER")
                .orElseThrow();

        Profile profile = new Profile();
        profile.setUser(owner);
        profile.setUserType(ownerType);
        profile.setFirstName("Salon");
        profile.setLastName("Owner");
        profile.setIsActiveStylist(false);

        profileRepository.save(profile);
    }


    @Transactional
    public void seedServiceCategories() {
        if (serviceCategoryRepository.count() > 0) {
            return; // already seeded
        }

        serviceCategoryRepository.saveAll(List.of(
            createCategory("Haircut", "All haircut services"),
            createCategory("Color", "Hair coloring services"),
            createCategory("Styling", "Styling and finishing services"),
            createCategory("Treatment", "Hair treatment services"),
            createCategory("Chemical", "Chemical texture services"),
            createCategory("Extensions", "Extension services"),
            createCategory("Add-Ons", "Add-on services")
        ));
    }



    private ServiceCategory createCategory(
            String name,
            String description
    ) {
        ServiceCategory category = new ServiceCategory();
        category.setCategoryName(name);
        category.setDescription(description);
        return category;
    }

    @Transactional
    public void seedServices() {
        if (servicesRepository.count() > 0) {
            return; // already seeded
        }

        ServiceCategory haircut = serviceCategoryRepository.findByCategoryName("Haircut").orElseThrow();
        ServiceCategory color = serviceCategoryRepository.findByCategoryName("Color").orElseThrow();
        ServiceCategory styling = serviceCategoryRepository.findByCategoryName("Styling").orElseThrow();
        ServiceCategory treatment = serviceCategoryRepository.findByCategoryName("Treatment").orElseThrow();
        ServiceCategory chemical = serviceCategoryRepository.findByCategoryName("Chemical").orElseThrow();
        ServiceCategory extensions = serviceCategoryRepository.findByCategoryName("Extensions").orElseThrow();
        ServiceCategory addons = serviceCategoryRepository.findByCategoryName("Add-Ons").orElseThrow();

        servicesRepository.saveAll(List.of(
            service(haircut, "Women's Cut", "Women's haircut with basic style", 45, 60),
            service(haircut, "Men's Cut", "Men's haircut", 25, 30),
            service(haircut, "Kids Cut (12 & under)", "Children's haircut", 15, 30),
            service(haircut, "Beard Trim", "Beard shaping and trim", 15, 15),
            service(haircut, "Bang Trim", "Fast bang cleanup for maintained style", 10, 10),

            service(color, "Full Color", "All-over permanent hair color", 90, 120),
            service(color, "Root Touch-Up", "Coloring roots only", 65, 90),
            service(color, "Highlights", "Partial highlights", 110, 150),
            service(color, "Balayage", "Hand-painted balayage color", 140, 180),

            service(styling, "Shampoo + Style", "Wash and basic style", 35, 45),
            service(styling, "Blowout", "Smooth blowout", 40, 60),
            service(styling, "Updo / Event Style", "Special occasion styling", 70, 90),
            service(styling, "Braids", "Protective and stylish braids", 210, 240),

            service(treatment, "Deep Conditioning", "Moisturizing treatment", 30, 30),
            service(treatment, "Keratin Treatment", "Smoothing treatment", 200, 180),

            service(chemical, "Perm", "Full perm service", 95, 150),
            service(chemical, "Partial Perm", "Partial perm service", 70, 120),

            service(extensions, "Extensions Installation", "Install extensions", 250, 180),
            service(extensions, "Extensions Maintenance", "Maintenance and tightening", 120, 90),

            service(addons, "Scalp Massage", "Relaxing scalp massage add-on", 15, 15),
            service(addons, "Clarifying Treatment", "Clarifying detox treatment", 20, 20)
        ));
    }

    private Services service(
            ServiceCategory category,
            String name,
            String description,
            int price,
            int duration
    ) {
        Services s = new Services();
        s.setCategory(category);
        s.setName(name);
        s.setDescription(description);
        s.setBasePrice(BigDecimal.valueOf(price));
        s.setTypicalDurationMinutes(duration);
        s.setIsAvailable(true);
        return s;
    }

    @Transactional
    public void seedBusinessHours() {
        if (businessHoursRepository.count() > 0) {
            return; // already seeded
        }

        businessHoursRepository.saveAll(List.of(
            createClosedDay(0), // Sunday
            createOpenDay(1, LocalTime.of(9, 0), LocalTime.of(19, 0)), // Monday
            createOpenDay(2, LocalTime.of(9, 0), LocalTime.of(19, 0)), // Tuesday
            createOpenDay(3, LocalTime.of(9, 0), LocalTime.of(19, 0)), // Wednesday
            createOpenDay(4, LocalTime.of(9, 0), LocalTime.of(19, 0)), // Thursday
            createOpenDay(5, LocalTime.of(9, 0), LocalTime.of(19, 0)), // Friday
            createOpenDay(6, LocalTime.of(9, 0), LocalTime.of(17, 0))  // Saturday
        ));
    }

    private BusinessHours createOpenDay(int dayOfWeek, LocalTime open, LocalTime close) {
        BusinessHours bh = new BusinessHours();
        bh.setDayOfWeek(dayOfWeek);
        bh.setOpenTime(open);
        bh.setCloseTime(close);
        bh.setIsClosed(false);
        return bh;
    }

    private BusinessHours createClosedDay(int dayOfWeek) {
        BusinessHours bh = new BusinessHours();
        bh.setDayOfWeek(dayOfWeek);
        bh.setIsClosed(true);
        bh.setOpenTime(null);
        bh.setCloseTime(null);
        return bh;
    }

    @Transactional
    public void seedStylists() {

        UserType stylistType = userTypeRepository
                .findByTypeNameIgnoreCase("STYLIST")
                .orElseThrow(() -> new IllegalStateException("STYLIST type not found"));

        seedStylist(
                "sam.rivera@lilliputsalon.com",
                "Sam",
                "Rivera",
                "555-1001",
                "Straight",
                "Medium",
                "Prefers color services",
                stylistType
        );

        seedStylist(
                "jamie.lee@lilliputsalon.com",
                "Jamie",
                "Lee",
                "555-1002",
                "Wavy",
                "Long",
                "Loves creative cuts",
                stylistType
        );

        seedStylist(
                "taylor.nguyen@lilliputsalon.com",
                "Taylor",
                "Nguyen",
                "555-1003",
                "Curly",
                "Short",
                "Good with kids",
                stylistType
        );

        seedStylist(
                "casey.patel@lilliputsalon.com",
                "Casey",
                "Patel",
                "555-1004",
                "Coily",
                "Medium",
                "Specializes in treatments",
                stylistType
        );

        seedStylist(
                "morgan.davis@lilliputsalon.com",
                "Morgan",
                "Davis",
                "555-1005",
                "Straight",
                "Long",
                "Excellent at styling",
                stylistType
        );
    }

    private void seedStylist(
            String email,
            String firstName,
            String lastName,
            String phone,
            String hairType,
            String hairLength,
            String preferences,
            UserType stylistType
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            return; // stylist already exists
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("StylistPassword!123"));
        user.setIsActive(true);

        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUserType(stylistType);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhone(phone);
        profile.setHairType(hairType);
        profile.setHairLength(hairLength);
        profile.setPreferences(preferences);
        profile.setIsActiveStylist(true);

        profileRepository.save(profile);
    }

    @Transactional
    public void seedAvailability() {

        // Get all stylist users
        List<User> stylists = userRepository.findAllStylists();
        // You likely already have this — if not, I’ll show you

        for (User stylist : stylists) {

            // Skip if availability already exists
            if (availabilityRepository.existsByUser(stylist)) {
                continue;
            }

            // Generate 8 weeks of availability
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusWeeks(8);

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

                // Skip Sundays
                if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    continue;
                }

                Availability availability = new Availability();
                availability.setUser(stylist);
                availability.setWorkDate(date);
                availability.setIsAvailable(true);

                // Different schedules per stylist (matches your table)
                applyStylistHours(stylist, availability);

                availabilityRepository.save(availability);

                seedBreakTime(availability);
            }
        }
    }

    private void applyStylistHours(User stylist, Availability availability) {

        String email = stylist.getEmail();

        if (email.contains("sam")) {
            availability.setDayStartTime(LocalTime.of(9, 0));
            availability.setDayEndTime(LocalTime.of(17, 0));

        } else if (email.contains("jamie")) {
            availability.setDayStartTime(LocalTime.of(12, 0));
            availability.setDayEndTime(LocalTime.of(19, 0));

        } else if (email.contains("taylor")) {
            availability.setDayStartTime(LocalTime.of(11, 0));
            availability.setDayEndTime(LocalTime.of(19, 0));

        } else {
            // default stylist hours
            availability.setDayStartTime(LocalTime.of(9, 0));
            availability.setDayEndTime(LocalTime.of(17, 0));
        }
    }

    @Transactional
    public void seedBreakTime(Availability availability) {

        BreakTime breakTime = new BreakTime();
        breakTime.setAvailability(availability);
        breakTime.setBreakType("Break");

        LocalTime start = availability.getDayStartTime().plusHours(4);
        breakTime.setBreakStartTime(start);
        breakTime.setBreakEndTime(start.plusMinutes(30));

        breakTimeRepository.save(breakTime);
    }





    @Transactional
    public void seedDemoCustomer() {
        if (userRepository.findByEmail("demo@customer.com").isPresent()) {
			return;
		}

        User customer = new User();
        customer.setEmail("demo@customer.com");
        customer.setPasswordHash(passwordEncoder.encode("customer123"));
        customer.setIsActive(true);

        userRepository.save(customer);
    }

    @Transactional
    public void seedDemoAppointment() {
        if (appointmentRepository.count() > 0) {
			return;
		}

        User customer = userRepository.findByEmail("demo@customer.com").orElseThrow();
        User stylist = userRepository.findByEmail("sam.rivera@lilliputsalon.com").orElseThrow();

        Appointment appt = new Appointment();
        appt.setCustomerId(customer.getId());
        appt.setStylistId(stylist.getId());
        appt.setScheduledStartDateTime(
            LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
        );
        appt.setDurationMinutes(60);
        appt.setStatus("SCHEDULED");
        appt.setBaseAmount(new BigDecimal("45.00"));
        appt.setDiscountAmount(BigDecimal.ZERO);
        appt.setTotalAmount(new BigDecimal("45.00"));
        appt.setIsCompleted(false);

        appointmentRepository.save(appt);
    }














}
