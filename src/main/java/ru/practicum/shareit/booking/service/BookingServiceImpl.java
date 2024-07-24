package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingShortDto bookingShortDto) {
        log.info("booking create");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        Item item = itemRepository.findById(bookingShortDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("item not found"));

        User owner = item.getOwner();

        if (owner.getId().equals(userId)) {
            throw new UserNotFoundException("user id " + userId + " not found");
        }

        if (!item.getAvailable()) {
            throw new NotAvailableException("item " + item.getName() + " not available");
        }

        if (bookingShortDto.getStart() == null ||
                bookingShortDto.getEnd() == null ||
                bookingShortDto.getStart().isBefore(LocalDateTime.now()) ||
                bookingShortDto.getStart().isAfter(bookingShortDto.getEnd()) ||
                bookingShortDto.getStart().isEqual(bookingShortDto.getEnd())) {
            throw new WrongDateException("date error exception");
        }
        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .start(bookingShortDto.getStart())
                .end(bookingShortDto.getEnd())
                .bookingStatus(BookingStatus.WAITING)
                .build();
        bookingRepository.save(booking);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        bookingDto.setState(State.WAITING);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto updateStatus(Long userId, Long bookingId, Boolean isApproved) {
        log.info("booking update status");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("booking not found"));
        if (booking.getBookingStatus().equals(BookingStatus.APPROVED)
                || booking.getBookingStatus().equals(BookingStatus.REJECTED)) {
            throw new NotAllowedActionException("Not allowed");
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new InvalidUserException("invalid user operation");
        }

        BookingStatus bookingStatus = isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setBookingStatus(bookingStatus);
        bookingRepository.save(booking);

        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        if (!isApproved) {
            bookingDto.setState(State.REJECTED);
        }

        return bookingDto;
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        log.info("get booking");
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("booking not found"));
        Item item = booking.getItem();
        User owner = item.getOwner();
        User booker = booking.getBooker();
        if (!owner.getId().equals(userId) && !booker.getId().equals(userId)) {
            throw new InvalidUserException("invalid user operation");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUsersBookings(Long userId, String value) {
        State state = validateState(value);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        List<Booking> foundBooking;
        switch (state) {
            case FUTURE:
                foundBooking = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case CURRENT:
                foundBooking = bookingRepository
                        .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                                LocalDateTime.now(),
                                LocalDateTime.now());
                break;
            case PAST:
                foundBooking = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case WAITING:
                foundBooking = bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(userId,
                        BookingStatus.WAITING);
                break;
            case REJECTED:
                foundBooking = bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(userId,
                        BookingStatus.REJECTED);
                break;
            default:
                foundBooking = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
        }
        return foundBooking.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnersBookings(Long userId, String value) {
        State state = validateState(value);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        List<Booking> foundBooking;
        switch (state) {
            case FUTURE:
                foundBooking = bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case CURRENT:
                foundBooking = bookingRepository
                        .findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                                LocalDateTime.now(),
                                LocalDateTime.now());
                break;
            case PAST:
                foundBooking = bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case WAITING:
                foundBooking = bookingRepository.findAllByItemOwnerIdAndBookingStatusOrderByStartDesc(userId,
                        BookingStatus.WAITING);
                break;
            case REJECTED:
                foundBooking = bookingRepository.findAllByItemOwnerIdAndBookingStatusOrderByStartDesc(userId,
                        BookingStatus.REJECTED);
                break;
            default:
                foundBooking = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
        }
        return foundBooking.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private State validateState(String value) throws InvalidStatusException {
        State state;
        try {
            state = State.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Unknown state: " + value);
        }
        return state;
    }
}
