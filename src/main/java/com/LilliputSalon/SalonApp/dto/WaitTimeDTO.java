package com.LilliputSalon.SalonApp.dto;

import java.time.LocalTime;

public record WaitTimeDTO(
	    int minutes,
	    String stylistName,
	    LocalTime availableAt
	) {}
