package com.cristopher.reservas_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class CourtAvailabilityResponse {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}