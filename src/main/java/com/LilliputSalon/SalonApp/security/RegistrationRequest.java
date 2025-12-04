package com.LilliputSalon.SalonApp.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
	
	@NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;
	
	@NotBlank(message = "Password is required")
    @Size(min = 5, message = "Password must be at least 5 characters long")
    private String password;
	
	@NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 40, message = "First name must be between 2–40 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 40, message = "Last name must be between 2–40 characters")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(
    	    regexp = "^\\d{3}-\\d{4}$|^\\d{3}-\\d{3}-\\d{4}$",
    	    message = "Phone must be in 555-5555 or 555-555-5555 format")
    private String phone;

}