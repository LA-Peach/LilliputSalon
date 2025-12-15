package com.LilliputSalon.SalonApp.security;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AppointmentOverlapException.class)
    public ResponseEntity<?> handleOverlap(AppointmentOverlapException ex) {
        return ResponseEntity.badRequest().body(
            Map.of(
                "status", "error",
                "message", ex.getMessage()
            )
        );
    }

    @ExceptionHandler(AppointmentAvailabilityException.class)
    public ResponseEntity<?> handleAvailability(AppointmentAvailabilityException ex) {
        return ResponseEntity.badRequest().body(
            Map.of(
                "status", "error",
                "message", ex.getMessage()
            )
        );
    }

}
