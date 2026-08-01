package com.cristopher.reservas_api.service;

import com.cristopher.reservas_api.dto.response.CourtAvailabilityResponse;
import com.cristopher.reservas_api.entity.Reservation;
import com.cristopher.reservas_api.entity.ReservationStatus;
import com.cristopher.reservas_api.exception.ResourceNotFoundException;
import com.cristopher.reservas_api.repository.CourtRepository;
import com.cristopher.reservas_api.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(23, 0);
    private static final int SLOT_DURATION_MINUTES = 60;

    private final CourtRepository courtRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<CourtAvailabilityResponse> getAvailability(Long courtId, LocalDate date) {

        courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada"));

        List<Reservation> reservations = reservationRepository
                .findByCourtIdAndReservationDate(courtId, date)
                .stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .toList();

        List<CourtAvailabilityResponse> slots = new ArrayList<>();
        LocalTime currentStart = OPENING_TIME;

        while (currentStart.plusMinutes(SLOT_DURATION_MINUTES).compareTo(CLOSING_TIME) <= 0) {
            final LocalTime slotStart = currentStart;
            final LocalTime slotEnd = currentStart.plusMinutes(SLOT_DURATION_MINUTES);

            boolean occupied = reservations.stream().anyMatch(r ->
                    slotStart.isBefore(r.getEndTime()) && slotEnd.isAfter(r.getStartTime())
            );

            slots.add(CourtAvailabilityResponse.builder()
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .available(!occupied)
                    .build());

            currentStart = slotEnd;
        }

        return slots;
    }
}