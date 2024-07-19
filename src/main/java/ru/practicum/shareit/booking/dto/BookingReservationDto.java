package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingReservationDto {
    private Long id;

    private Long bookerId;

    private LocalDateTime start;

    private LocalDateTime end;
}
