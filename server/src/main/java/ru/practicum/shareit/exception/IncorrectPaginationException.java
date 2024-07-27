package ru.practicum.shareit.exception;

public class IncorrectPaginationException extends RuntimeException {
    public IncorrectPaginationException(String message) {
        super(message);
    }
}
