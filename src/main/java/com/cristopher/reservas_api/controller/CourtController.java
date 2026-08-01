package com.cristopher.reservas_api.controller;

import com.cristopher.reservas_api.dto.response.CourtAvailabilityResponse;
import com.cristopher.reservas_api.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/courts")
@RequiredArgsConstructor
public class CourtController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<CourtAvailabilityResponse>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(availabilityService.getAvailability(id, date));
    }
}