package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingShortDto bookingShortDto);

    BookingDto updateStatus(Long userId, Long bookingId, Boolean isApproved);

    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getUsersBookings(Long userId, String state, Integer from, Integer size);

    List<BookingDto> getOwnersBookings(Long userId, String state, Integer from, Integer size);

}
