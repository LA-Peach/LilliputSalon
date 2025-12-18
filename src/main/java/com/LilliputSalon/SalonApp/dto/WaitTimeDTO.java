package com.LilliputSalon.SalonApp.dto;

import java.time.LocalTime;
import java.util.List;

public record WaitTimeDTO(
		WalkInStatus status,
	    int minutes,
	    String stylistName,
	    LocalTime availableAt,
	    List<String> availableStylists
	) {}


