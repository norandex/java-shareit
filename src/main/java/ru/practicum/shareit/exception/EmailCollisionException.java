package ru.practicum.shareit.exception;

public class EmailCollisionException extends RuntimeException {
    public EmailCollisionException(String message) {
        super(message);
    }
}