package com.cristopher.reservas_api.repository;

import com.cristopher.reservas_api.entity.Reservation;
import com.cristopher.reservas_api.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByCourtIdAndReservationDate(Long courtId, LocalDate date);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.court.id = :courtId
        AND r.reservationDate = :date
        AND r.status <> :cancelledStatus
        AND r.startTime < :endTime
        AND r.endTime > :startTime
        """)
    List<Reservation> findOverlapping(
            @Param("courtId") Long courtId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("cancelledStatus") ReservationStatus cancelledStatus
    );
}