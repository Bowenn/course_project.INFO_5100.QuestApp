package edu.info5100.questapp.exception;

/**
 * Thrown when a request is invalid (e.g. wrong status transition, permission denied).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
