package ru.practicum.shareit.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String message) {
        super(message);
    }
}