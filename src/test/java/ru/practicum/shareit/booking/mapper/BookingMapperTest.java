package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingReservationDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {

    @Test
    void toBookingInfoDtoTest() {
        User user = User.builder()
                .id(1L)
                .name("user name")
                .email("user@mail.ru")
                .build();
        UserDto userDto = UserMapper.toUserDto(user);
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .build();
        ItemDto itemDto = ItemMapper.toItemDto(item);
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .bookingStatus(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(14))
                .booker(user)
                .build();

        BookingDto bookingDto =
                BookingMapper
                        .toBookingDto(booking);

        assertEquals(bookingDto.getItem().getName(), booking.getItem().getName());
        assertEquals(bookingDto.getBooker().getName(), booking.getBooker().getName());
        assertEquals(bookingDto.getStatus(), booking.getBookingStatus());
        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());
    }

    @Test
    void toBookingReservationDtoTest() {
        User user = User.builder()
                .id(1L)
                .name("user name")
                .email("user@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .bookingStatus(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(14))
                .booker(user)
                .build();

        BookingReservationDto lastBookingDto = BookingMapper.toBookingReservationDto(booking);
        assertEquals(lastBookingDto.getBookerId(), booking.getBooker().getId());
        assertEquals(lastBookingDto.getStart(), booking.getStart());
        assertEquals(lastBookingDto.getEnd(), booking.getEnd());
    }
}
