package com.LilliputSalon.SalonApp.dto;

import java.util.List;
import lombok.Data;

@Data
public class WalkInQueueDTO {
    private Integer walkInId;
    private Long customerId;
    private String customerName;
    private Integer estimatedWaitMinutes;
    private List<String> services;
}
