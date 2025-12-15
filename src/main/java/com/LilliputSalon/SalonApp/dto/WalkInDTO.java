package com.LilliputSalon.SalonApp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WalkInDTO {

    private Integer walkInId;

    private Long customerId;

    private LocalDateTime timeEntered;

    private Integer estimatedWaitMinutes;

    private Integer assignedStylistId;

    private String status;

    private List<WalkInRequestedServiceDTO> services;
}
