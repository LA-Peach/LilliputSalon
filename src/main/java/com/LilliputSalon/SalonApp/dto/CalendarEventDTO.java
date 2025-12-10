package com.LilliputSalon.SalonApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CalendarEventDTO {
    private Integer appointmentId;
    private String title;
    private String start;
    private String end;
    private Integer stylistId;
}
