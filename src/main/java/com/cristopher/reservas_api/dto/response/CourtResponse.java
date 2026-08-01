package com.cristopher.reservas_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CourtResponse {
    private Long id;
    private String name;
    private String sport;
    private BigDecimal pricePerHour;
}