package com.cristopher.reservas_api.controller;

import com.cristopher.reservas_api.dto.request.CreateReservationRequest;
import com.cristopher.reservas_api.dto.response.ReservationResponse;
import com.cristopher.reservas_api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        ReservationResponse response = reservationService.createReservation(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(reservationService.getMyReservations(userEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        reservationService.cancelReservation(userEmail, id);
        return ResponseEntity.noContent().build();
    }
}