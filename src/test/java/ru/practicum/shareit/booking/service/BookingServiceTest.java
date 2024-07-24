package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }

    @Test
    void createBookingTest() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user)
                .build();

        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        LocalDateTime start = LocalDateTime.now().plusMonths(1L);
        LocalDateTime end = LocalDateTime.now().plusMonths(2L);

        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .bookingStatus(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDto bookingDto = bookingService.createBooking(3L, bookingShortDto);
        assertNotNull(bookingDto);
        assertEquals(bookingDto.getBooker().getName(), booking.getBooker().getName());
        assertEquals(bookingDto.getItem().getName(), booking.getItem().getName());
    }

    @Test
    void createBookingThrowsUserNotFoundException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.createBooking(1L, bookingShortDto));
        assertEquals(userNotFoundException.getMessage(), "user not found");
    }

    @Test
    void createBookingThrowsItemNotFoundException() {
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> bookingService.createBooking(1L, bookingShortDto));
        assertEquals(itemNotFoundException.getMessage(), "item not found");
    }

    @Test
    void createBookingThrowsOwnerUserNotFoundException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.createBooking(1L, bookingShortDto));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void createBookingThrowsNotAvailableException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("description")
                .available(false)
                .owner(owner)
                .build();
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        NotAvailableException notAvailableException = assertThrows(NotAvailableException.class,
                () -> bookingService.createBooking(3L, bookingShortDto));
        assertEquals(notAvailableException.getMessage(), "item item1 not available");
    }

    @Test
    void createBookingStartIsAfterEndThrowsWrongDateException() {
        LocalDateTime start = LocalDateTime.now().plusDays(30L);
        LocalDateTime end = LocalDateTime.now().plusDays(4L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        WrongDateException wrongDateException = assertThrows(WrongDateException.class,
                () -> bookingService.createBooking(3L, bookingShortDto));
        assertEquals(wrongDateException.getMessage(), "date error exception");
    }

    @Test
    void createBookingStartIsEqualsEndThrowsWrongDateException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));

        WrongDateException wrongDateException = assertThrows(WrongDateException.class,
                () -> bookingService.createBooking(3L, bookingShortDto));
        assertEquals(wrongDateException.getMessage(), "date error exception");
    }

    @Test
    void updateStatusIsApprovedTest() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user)
                .build();

        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        LocalDateTime start = LocalDateTime.now().plusMonths(1L);
        LocalDateTime end = LocalDateTime.now().plusMonths(2L);

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .bookingStatus(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findById(any()))
                .thenReturn(Optional.of(booking));

        BookingDto bookingDto = bookingService
                .updateStatus(1L, 1L, true);
        assertNotNull(bookingDto);
        assertEquals(bookingDto.getStatus(), BookingStatus.APPROVED);
    }

    @Test
    void updateStatusIsNotApprovedTest() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user)
                .build();

        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        LocalDateTime start = LocalDateTime.now().plusMonths(1L);
        LocalDateTime end = LocalDateTime.now().plusMonths(2L);

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .bookingStatus(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findById(any()))
                .thenReturn(Optional.of(booking));

        BookingDto bookingDto = bookingService
                .updateStatus(1L, 1L, false);
        assertNotNull(bookingDto);
        assertEquals(bookingDto.getStatus(), BookingStatus.REJECTED);
    }

    @Test
    void updateStatusThrowsUserNotFoundException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.updateStatus(1L, 1L, true));
        assertEquals(userNotFoundException.getMessage(), "user not found");
    }

    @Test
    void updateStatusThrowsBookingNotFoundException() {
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        BookingNotFoundException bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.updateStatus(3L, 1L, true));
        assertEquals(bookingNotFoundException.getMessage(), "booking not found");
    }

    @Test
    void updateStatusWhenBookingIsAlreadyApprovedThrowsNotAllowedStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        NotAllowedActionException notAllowedActionException = assertThrows(NotAllowedActionException.class,
                () -> bookingService.updateStatus(3L, 1L, true));
        assertEquals(notAllowedActionException.getMessage(), "Not allowed");
    }

    @Test
    void updateStatusWhenBookingIsRejectedThrowsNotAllowedStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.REJECTED)
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        NotAllowedActionException notAllowedStatusException = assertThrows(NotAllowedActionException.class,
                () -> bookingService.updateStatus(3L, 1L, true));
        assertEquals(notAllowedStatusException.getMessage(), "Not allowed");
    }

    @Test
    void updateStatusThrowsInvalidUserException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.WAITING)
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class,
                () -> bookingService.updateStatus(3L, 1L, true));
        assertEquals(invalidUserException.getMessage(), "invalid user operation");
    }

    @Test
    void getBookingTest() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user)
                .build();

        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        LocalDateTime start = LocalDateTime.now().plusMonths(1L);
        LocalDateTime end = LocalDateTime.now().plusMonths(2L);

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .bookingStatus(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        BookingDto bookingDto = bookingService.getBooking(1L, 1L);
        assertNotNull(bookingDto);
    }

    @Test
    void getBookingThrowsBookingNotFoundException() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        BookingNotFoundException bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBooking(1L, 1L));
        assertEquals(bookingNotFoundException.getMessage(), "booking not found");
    }

    @Test
    void getBookingUserNotBookerOrItemOwnerThrowsInvalidUserException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        User anotherUser = User.builder()
                .id(4L)
                .name("user")
                .email("anotheruser@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(4L))
                .thenReturn(Optional.of(anotherUser));

        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class,
                () -> bookingService.getBooking(4L, 1L));
        assertEquals(invalidUserException.getMessage(), "invalid user operation");
    }

    @Test
    void getUsersAllBookings() {
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(booker));
        LocalDateTime startFuture = LocalDateTime.now().plusYears(1L);
        LocalDateTime endFuture = LocalDateTime.now().plusYears(5L);
        Booking futureBooking = Booking.builder()
                .id(1L)
                .start(startFuture)
                .end(endFuture)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();
        LocalDateTime startPast = LocalDateTime.now().minusYears(3L);
        LocalDateTime endPast = LocalDateTime.now().minusYears(2L);
        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(startPast)
                .end(endPast)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();
        LocalDateTime startPresent = LocalDateTime.now().minusDays(3L);
        LocalDateTime endPresent = LocalDateTime.now().plusDays(2L);
        Booking presentBooking = Booking.builder()
                .id(1L)
                .start(startPresent)
                .end(endPresent)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByBookerIdOrderByStartDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(futureBooking, pastBooking, presentBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getUsersBookings(3L, State.ALL.toString(), 0, 11);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 3);
    }

    @Test
    void getUsersFutureBookings() {
        LocalDateTime startFuture = LocalDateTime.now().plusYears(1L);
        LocalDateTime endFuture = LocalDateTime.now().plusYears(5L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(booker));
        Booking futureBooking = Booking.builder()
                .id(1L)
                .start(startFuture)
                .end(endFuture)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(futureBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getUsersBookings(3L, State.FUTURE.toString(), 0, 11);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getUsersPastBookings() {

        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(booker));
        LocalDateTime startPast = LocalDateTime.now().minusDays(3L);
        LocalDateTime endPast = LocalDateTime.now().minusDays(2L);
        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(startPast)
                .end(endPast)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(pastBooking)));

        List<BookingDto> bookingInfoDtoList = bookingService
                .getUsersBookings(3L, State.PAST.toString(), 0, 11);
        assertFalse(bookingInfoDtoList.isEmpty());
        assertEquals(bookingInfoDtoList.size(), 1);
    }

    @Test
    void getUsersCurrentBookings() {

        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(booker));
        LocalDateTime startCurrent = LocalDateTime.now().minusDays(3L);
        LocalDateTime endCurrent = LocalDateTime.now().plusDays(2L);
        Booking currentBooking = Booking.builder()
                .id(1L)
                .start(startCurrent)
                .end(endCurrent)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(currentBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getUsersBookings(3L, State.CURRENT.toString(), 0, 11);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getUsersWaitingBookings() {
        LocalDateTime start = LocalDateTime.now().minusDays(3L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(booker));
        Booking waitingBooking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(waitingBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getUsersBookings(3L, State.WAITING.toString(), 0, 11);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getUserRejectedBookings() {
        LocalDateTime start = LocalDateTime.now().minusDays(3L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(booker));
        Booking rejectedBooking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.REJECTED)
                .build();

        when(bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(rejectedBooking)));

        List<BookingDto> bookingInfoDtoList = bookingService
                .getUsersBookings(3L, State.REJECTED.toString(), 0, 11);
        assertFalse(bookingInfoDtoList.isEmpty());
        assertEquals(bookingInfoDtoList.size(), 1);
    }

    @Test
    void getUsersBookingsThrowsInvalidStatusException() {
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        InvalidStatusException invalidStatusException = assertThrows(InvalidStatusException.class,
                () -> bookingService.getUsersBookings(3L, "UNKNOWN_STATE", 0, 11));
        assertEquals(invalidStatusException.getMessage(), "Unknown state: UNKNOWN_STATE");
    }

    @Test
    void getUsersBookingsThrowsUserNotFoundException() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException notFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUsersBookings(1L, "ALL", 0, 11));
        assertEquals(notFoundException.getMessage(), "user not found");

        notFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUsersBookings(1L, "CURRENT", 0, 11));
        assertEquals(notFoundException.getMessage(), "user not found");
        notFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUsersBookings(1L, "PAST", 0, 11));
        assertEquals(notFoundException.getMessage(), "user not found");
        notFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUsersBookings(1L, "FUTURE", 0, 11));
        assertEquals(notFoundException.getMessage(), "user not found");
        notFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUsersBookings(1L, "WAITING", 0, 11));
        assertEquals(notFoundException.getMessage(), "user not found");
        notFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUsersBookings(1L, "REJECTED", 0, 11));
        assertEquals(notFoundException.getMessage(), "user not found");
    }

    @Test
    void getUsersBookingsThrowsIncorrectPaginationException() {
        User owner = User.builder()
                .id(1L)
                .name("owner name")
                .email("owner@mail.ru")
                .build();
        User booker = User.builder()
                .id(2L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test name")
                .description("description")
                .owner(owner)
                .available(true)
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        IncorrectPaginationException incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "ALL", -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "ALL", 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "CURRENT", -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "CURRENT", 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "PAST", -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "PAST", 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "FUTURE", -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "FUTURE", 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "WAITING", -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "WAITING", 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "REJECTED", -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getUsersBookings(booker.getId(), "REJECTED", 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
    }

    //getOwnerBooking

    @Test
    void getOwnersAllBookingTest() {
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1L);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2L);
        LocalDateTime pastStart = LocalDateTime.now().minusDays(3L);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(2L);
        LocalDateTime currentStart = LocalDateTime.now().minusDays(3L);
        LocalDateTime currentEnd = LocalDateTime.now().minusDays(2L);

        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();

        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Booking futureBooking = Booking.builder()
                .id(1L)
                .start(futureStart)
                .end(futureEnd)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();
        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(pastStart)
                .end(pastEnd)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();
        Booking currentBooking = Booking.builder()
                .id(1L)
                .start(currentStart)
                .end(currentEnd)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(futureBooking, pastBooking, currentBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getOwnersBookings(1L, "ALL", 0, 11);
        Assertions.assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 3);
    }

    @Test
    void getOwnersFutureBookings() {
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1L);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Booking futureBooking = Booking.builder()
                .id(1L)
                .start(futureStart)
                .end(futureEnd)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(futureBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getOwnersBookings(1L, "FUTURE", 0, 11);
        Assertions.assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getOwnersPastBookings() {
        LocalDateTime pastStart = LocalDateTime.now().minusDays(3L);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Booking pastBooking = Booking.builder()
                .id(1L)
                .start(pastStart)
                .end(pastEnd)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(pastBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getOwnersBookings(1L, "PAST", 0, 11);
        Assertions.assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getOwnersCurrentBookings() {
        LocalDateTime currentStart = LocalDateTime.now().minusDays(3L);
        LocalDateTime currentEnd = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Booking currentBooking = Booking.builder()
                .id(1L)
                .start(currentStart)
                .end(currentEnd)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository
                .findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(currentBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getOwnersBookings(1L, "CURRENT", 0, 11);
        Assertions.assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getOwnersWaitingBookings() {
        LocalDateTime start = LocalDateTime.now().minusDays(3L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Booking waitingBooking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository
                .findAllByItemOwnerIdAndBookingStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(waitingBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getOwnersBookings(1L, "WAITING", 0, 11);
        Assertions.assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getOwnersRejectedBookingsTestOk() {
        LocalDateTime start = LocalDateTime.now().minusDays(3L);
        LocalDateTime end = LocalDateTime.now().plusDays(2L);
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        User booker = User.builder()
                .id(3L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        Booking rejectedBooking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(bookingRepository
                .findAllByItemOwnerIdAndBookingStatusOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(rejectedBooking)));

        List<BookingDto> bookingDtos = bookingService
                .getOwnersBookings(1L, "REJECTED", 0, 11);
        Assertions.assertFalse(bookingDtos.isEmpty());
        assertEquals(bookingDtos.size(), 1);
    }

    @Test
    void getOwnersBookingsThrowsInvalidStatusException() {
        User owner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();
        when(userRepository.existsById(any()))
                .thenReturn(true);
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(owner));
        InvalidStatusException stateException = assertThrows(InvalidStatusException.class,
                () -> bookingService.getOwnersBookings(1L, "UNKNOWN_STATE", 0, 11));
        assertEquals(stateException.getMessage(), "Unknown state: UNKNOWN_STATE");
    }

    @Test
    void getOwnersBookingsThrowsUserNotFoundException() {
        when(userRepository.existsById(any()))
                .thenReturn(false);
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> bookingService.getOwnersBookings(1L, "ALL", 0, 11));
        assertEquals(userNotFoundException.getMessage(), "user not found");
    }

    @Test
    void getOwnersBookingsThrowsIncorrectPaginationException() {
        User owner = User.builder()
                .id(1L)
                .name("owner name")
                .email("owner@mail.ru")
                .build();
        User booker = User.builder()
                .id(2L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("test name")
                .description("description")
                .owner(owner)
                .available(true)
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        IncorrectPaginationException paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "ALL", -1, 11));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "ALL", 0, 0));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "CURRENT", -1, 11));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "CURRENT", 0, 0));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "PAST", -1, 11));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "PAST", 0, 0));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "FUTURE", -1, 11));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "FUTURE", 0, 0));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "WAITING", -1, 11));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "WAITING", 0, 0));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "REJECTED", -1, 11));
        assertEquals(paginationException.getMessage(), "pagination error");
        paginationException = assertThrows(IncorrectPaginationException.class,
                () -> bookingService.getOwnersBookings(booker.getId(), "REJECTED", 0, 0));
        assertEquals(paginationException.getMessage(), "pagination error");
    }


}
