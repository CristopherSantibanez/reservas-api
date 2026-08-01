package com.cristopher.reservas_api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateReservationRequest {

    @NotNull(message = "La cancha es obligatoria")
    private Long courtId;

    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha debe ser futura")
    private LocalDate reservationDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
}