package com.LilliputSalon.SalonApp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NextAppointmentDTO {
    private LocalDateTime dateTime;
    private String clientName;   // for stylists
    private String providerName; // for customers
    private String serviceName;
}