package com.LilliputSalon.SalonApp.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class CalendarEventDTO {
    private Integer id;
    private String title;
    private String start;
    private String end;
    private Integer stylistId;
    private List<Long> serviceIds;
}
