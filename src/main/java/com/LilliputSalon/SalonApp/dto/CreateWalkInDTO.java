package com.LilliputSalon.SalonApp.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateWalkInDTO {

    // EXISTING customer (returning)
    private Long customerId;        // nullable

    // NEW customer (first-time)
    private NewCustomerDTO newCustomer; // nullable

    // Requested services (REQUIRED)
    private List<CreateWalkInServiceDTO> services;
}
