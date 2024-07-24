package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.exception.InvalidStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateModelTest {
    @Test
    void StateFromTest() {
        String stateString = "FUTURE";

        assertEquals(State.from(stateString).orElseThrow(() -> new InvalidStatusException("invalid Status")), State.FUTURE);
    }
}
