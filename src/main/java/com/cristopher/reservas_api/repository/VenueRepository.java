package com.cristopher.reservas_api.repository;

import com.cristopher.reservas_api.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}