package com.cristopher.reservas_api.service;

import com.cristopher.reservas_api.dto.response.CourtResponse;
import com.cristopher.reservas_api.dto.response.VenueResponse;
import com.cristopher.reservas_api.entity.Venue;
import com.cristopher.reservas_api.exception.ResourceNotFoundException;
import com.cristopher.reservas_api.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue no encontrado"));
        return mapToResponse(venue);
    }

    private VenueResponse mapToResponse(Venue venue) {
        List<CourtResponse> courts = venue.getCourts().stream()
                .map(c -> CourtResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .sport(c.getSport())
                        .pricePerHour(c.getPricePerHour())
                        .build())
                .toList();

        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .courts(courts)
                .build();
    }
}