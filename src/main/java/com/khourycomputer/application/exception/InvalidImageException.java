package com.khourycomputer.application.exception;

public class InvalidImageException
        extends IllegalArgumentException {

    public InvalidImageException(String message) {
        super(message);
    }

    public InvalidImageException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}