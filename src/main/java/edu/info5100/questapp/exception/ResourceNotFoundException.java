package edu.info5100.questapp.exception;

/**
 * Thrown when a requested resource (User, Task, Assignment) does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super("%s not found with id: %d".formatted(resource, id));
    }
}
