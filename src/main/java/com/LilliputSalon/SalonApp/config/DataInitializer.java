package com.LilliputSalon.SalonApp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            System.out.println("🌱 Database initialized");
        };
    }
}
