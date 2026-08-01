package com.cristopher.reservas_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private List<CourtResponse> courts;
}