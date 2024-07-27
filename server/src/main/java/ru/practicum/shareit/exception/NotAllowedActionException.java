package ru.practicum.shareit.exception;

public class NotAllowedActionException extends RuntimeException {
    public NotAllowedActionException(String message) {
        super(message);
    }
}
