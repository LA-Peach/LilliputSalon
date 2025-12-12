package com.LilliputSalon.SalonApp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateAppointmentDTO {
    private Integer stylistId;
    private String start;
    private String end;

    private String customerEmail;   // ✅ new (for existing customers)
    private String guestFirstName;  // ✅ optional (walk-in)
    private String guestLastName;   // ✅ optional
    private String guestPhone;      // ✅ optional
}
