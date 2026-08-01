package com.cristopher.reservas_api.exception;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(String message) {
        super(message);
    }
}