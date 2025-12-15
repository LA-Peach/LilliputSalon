package com.LilliputSalon.SalonApp.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WalkInRequestedServiceDTO {

    private Long serviceId;
    private Integer estimatedDurationMinutes;
    private BigDecimal estimatedPrice;

}
