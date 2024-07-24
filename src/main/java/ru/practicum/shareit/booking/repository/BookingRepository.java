package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndBookingStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndBookingStatusOrderByStartDesc(Long ownerId, BookingStatus bookingStatus);

    List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime start);

    List<Booking> findAllByItemOwnerIdAndBookingStatusIsAndStartBefore(Long ownerId,
                                                                       BookingStatus bookingStatus,
                                                                       LocalDateTime localDateTime,
                                                                       Sort sort);

    List<Booking> findAllByItemOwnerIdAndBookingStatusIsAndStartAfter(Long ownerId,
                                                                      BookingStatus bookingStatus,
                                                                      LocalDateTime localDateTime,
                                                                      Sort sort);

    Optional<Booking> findTopByItemIdAndBookerIdAndEndIsBeforeAndBookingStatusIs(Long itemId,
                                                                                 Long bookerId,
                                                                                 LocalDateTime endTime,
                                                                                 BookingStatus bookingStatus,
                                                                                 Sort sort);

}
