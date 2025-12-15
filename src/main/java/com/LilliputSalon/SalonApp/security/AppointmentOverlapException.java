package com.LilliputSalon.SalonApp.security;

public class AppointmentOverlapException extends RuntimeException {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public AppointmentOverlapException(String message) {
        super(message);
    }
}
