package com.cristopher.reservas_api.dto.response;

import com.cristopher.reservas_api.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private String userName;
    private String courtName;
    private String venueName;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ReservationStatus status;
}