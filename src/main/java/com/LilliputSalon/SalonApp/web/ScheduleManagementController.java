package com.LilliputSalon.SalonApp.web;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.LilliputSalon.SalonApp.dto.ScheduleEventDTO;
import com.LilliputSalon.SalonApp.service.ScheduleService;

@Controller
@PreAuthorize("hasRole('OWNER')")
@RequestMapping("/scheduleManagement")
public class ScheduleManagementController {

    private final ScheduleService service;

    public ScheduleManagementController(ScheduleService service) {
        this.service = service;
    }

    /* --------------------------------------------
       PAGE
       -------------------------------------------- */
    @GetMapping
    public String scheduleManagementPage(Model model) {
        model.addAttribute("stylists", service.getAllStylists());
        return "scheduleManagement";
    }

    /* --------------------------------------------
       BUSINESS HOURS
       -------------------------------------------- */
    @GetMapping("/businessHours")
    @ResponseBody
    public List<Map<String, Object>> getBusinessHours() {
        return service.getBusinessHours();
    }



    /* --------------------------------------------
       EVENTS (per stylist)
       -------------------------------------------- */
    @GetMapping("/events")
    @ResponseBody
    public List<ScheduleEventDTO> getEvents(@RequestParam Long stylistId) {
        return service.getCalendarEventsForStylist(stylistId);
    }

    @GetMapping("/allEvents")
    @ResponseBody
    public List<ScheduleEventDTO> getAllEvents() {
        return service.getAllCalendarEvents();
    }

    /* --------------------------------------------
       CREATE BLOCK (SHIFT or BREAK)
       -------------------------------------------- */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, String> create(@RequestBody Map<String, String> body) {
        try {
            Long stylistId = parseLong(body.get("stylistId"), "stylistId");
            String type = require(body.get("type"), "type");
            String start = require(body.get("start"), "start");
            String end = require(body.get("end"), "end");

            service.createBlock(type, start, end, stylistId);
            return ok();

        } catch (Exception ex) {
            return error(ex.getMessage());
        }
    }

    /* --------------------------------------------
       UPDATE BLOCK (drag / resize)
       -------------------------------------------- */
    @PostMapping("/update")
    @ResponseBody
    public Map<String, String> update(@RequestBody Map<String, String> body) {
        try {
            Long id = parseLong(body.get("id"), "id");
            Long stylistId = parseLong(body.get("stylistId"), "stylistId");
            String start = require(body.get("start"), "start");
            String end = require(body.get("end"), "end");

            boolean updated = service.updateBlock(id, stylistId, start, end);

            return updated
                    ? ok()
                    : error("Block not found");

        } catch (Exception ex) {
            return error(ex.getMessage());
        }
    }

    /* --------------------------------------------
       DELETE BLOCK (shift or break)
       -------------------------------------------- */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public Map<String, String> delete(@PathVariable Long id) {
        try {
            boolean deleted = service.deleteBlock(id);

            return deleted
                    ? ok()
                    : error("Block not found");

        } catch (Exception ex) {
            return error(ex.getMessage());
        }
    }

    /* --------------------------------------------
       HELPERS (clean code)
       -------------------------------------------- */

    private Map<String, String> ok() {
        return Map.of("status", "ok");
    }

    private Map<String, String> error(String message) {
        return Map.of("status", "error", "message", message);
    }

    private String require(String val, String field) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return val;
    }

    private Long parseLong(String val, String field) {
        try {
            return Long.parseLong(require(val, field));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number for " + field);
        }
    }

}
