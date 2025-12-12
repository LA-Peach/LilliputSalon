package com.LilliputSalon.SalonApp.dto;

import lombok.Getter;

@Getter
public class CalendarEventDTO {
    private Integer id;
    private String title;
    private String start;
    private String end;
    private Integer stylistId;
}
