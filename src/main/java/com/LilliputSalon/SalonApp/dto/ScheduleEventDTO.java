package com.LilliputSalon.SalonApp.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduleEventDTO {

    private Long id;
    private String title;
    private String start;
    private String end;
    private Map<String, Object> extendedProps;

    public ScheduleEventDTO(Long id, String title, String start, String end, String type, Long stylistId) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;

        this.extendedProps = Map.of(
            "type", type,
            "stylistId", stylistId
        );
    }


}




