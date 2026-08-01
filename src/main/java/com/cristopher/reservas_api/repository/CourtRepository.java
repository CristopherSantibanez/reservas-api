package com.cristopher.reservas_api.repository;

import com.cristopher.reservas_api.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {

    List<Court> findByVenueId(Long venueId);
}