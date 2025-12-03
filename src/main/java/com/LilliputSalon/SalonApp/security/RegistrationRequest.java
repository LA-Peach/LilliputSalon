package com.LilliputSalon.SalonApp.security;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {

    private String email;
    private String password;
    private String confirmPassword;

    private String firstName;
    private String lastName;
    @Pattern(
    	    regexp = "^\\d{3}-\\d{4}$|^\\d{3}-\\d{3}-\\d{4}$",
    	    message = "Phone must be in 555-5555 or 555-555-5555 format"
    	)
    	private String phone;

}