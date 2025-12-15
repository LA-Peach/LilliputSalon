package com.LilliputSalon.SalonApp.security;

public class AppointmentAvailabilityException extends RuntimeException {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public AppointmentAvailabilityException(String message) {
        super(message);
    }
}
