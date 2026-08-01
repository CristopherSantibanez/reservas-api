package com.cristopher.reservas_api.exception;

public class ReservationConflictException extends RuntimeException {
    public ReservationConflictException(String message) {
        super(message);
    }
}