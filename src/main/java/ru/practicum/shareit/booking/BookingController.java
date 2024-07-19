package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {


    private static final String USER_ID = "X-Sharer-User-Id";

    private final BookingService bookingService;


    @PostMapping
    public BookingDto createBooking(@RequestHeader(USER_ID) Long userId,
                                    @RequestBody BookingShortDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader(USER_ID) Long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        return bookingService.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(USER_ID) Long userId, @PathVariable Long bookingId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUsersBookings(@RequestHeader(USER_ID) Long userId,
                                             @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getUsersBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnersBookings(@RequestHeader(USER_ID) Long userId,
                                              @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getOwnersBookings(userId, state);
    }
}
