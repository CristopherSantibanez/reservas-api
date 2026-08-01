package com.cristopher.reservas_api.service;

import com.cristopher.reservas_api.dto.request.CreateReservationRequest;
import com.cristopher.reservas_api.dto.response.ReservationResponse;
import com.cristopher.reservas_api.entity.*;
import com.cristopher.reservas_api.exception.*;
import com.cristopher.reservas_api.repository.CourtRepository;
import com.cristopher.reservas_api.repository.ReservationRepository;
import com.cristopher.reservas_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ReservationResponse createReservation(String userEmail, CreateReservationRequest request) {

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidReservationException("La hora de fin debe ser posterior a la hora de inicio");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada"));

        List<Reservation> overlapping = reservationRepository.findOverlapping(
                request.getCourtId(),
                request.getReservationDate(),
                request.getStartTime(),
                request.getEndTime(),
                ReservationStatus.CANCELLED
        );

        if (!overlapping.isEmpty()) {
            throw new ReservationConflictException(
                    "La cancha ya está reservada en ese horario");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .court(court)
                .reservationDate(request.getReservationDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return reservationRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void cancelReservation(String userEmail, Long reservationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        boolean isOwner = reservation.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new InvalidReservationException("No tienes permiso para cancelar esta reserva");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .userName(r.getUser().getName())
                .courtName(r.getCourt().getName())
                .venueName(r.getCourt().getVenue().getName())
                .reservationDate(r.getReservationDate())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus())
                .build()
                ;
    }
}