package com.LilliputSalon.SalonApp.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.LilliputSalon.SalonApp.domain.Appointment;
import com.LilliputSalon.SalonApp.domain.BusinessHours;
import com.LilliputSalon.SalonApp.domain.WalkIn;
import com.LilliputSalon.SalonApp.dto.CreateWalkInDTO;
import com.LilliputSalon.SalonApp.dto.WalkInQueueDTO;
import com.LilliputSalon.SalonApp.repository.BusinessHoursRepository;
import com.LilliputSalon.SalonApp.service.WalkInManagerService;

@RestController
@RequestMapping("/walkins")
@PreAuthorize("hasAnyRole('OWNER','STYLIST')")
public class WalkInController {

    private final WalkInManagerService service;
    private final BusinessHoursRepository bhRepo;

    public WalkInController(WalkInManagerService service,
    		BusinessHoursRepository bhRepo) {
        this.service = service;
        this.bhRepo = bhRepo;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateWalkInDTO dto) {
        service.create(dto);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }


    @GetMapping("/queue")
    public List<WalkInQueueDTO> queue() {
        return service.getQueueDTOs();
    }


    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        service.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startWalkIn(
            @PathVariable Integer id,
            @RequestParam Integer stylistId
    ) {
        BusinessHours bh = bhRepo.findById(1)
            .orElseThrow();

        Appointment appt =
        	    service.convertToAppointment(id, stylistId);

        return ResponseEntity.ok(
            Map.of(
                "status", "ok",
                "appointmentId", appt.getAppointmentId(),
                "start", appt.getScheduledStartDateTime()
            )
        );
    }



}
