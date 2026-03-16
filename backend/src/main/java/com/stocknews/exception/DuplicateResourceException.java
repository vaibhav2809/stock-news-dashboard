package com.stocknews.exception;

/**
 * Thrown when an operation would create a duplicate resource
 * (e.g., a space with the same name, an article already saved to a space).
 * Results in HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
