package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, PageRequest pageRequest);

    Page<Booking> findAllByBookerIdAndBookingStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus, PageRequest pageRequest);

    Page<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, LocalDateTime end, PageRequest pageRequest);

    Page<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, PageRequest pageRequest);

    Page<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start, PageRequest pageRequest);

    Page<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId, PageRequest pageRequest);

    Page<Booking> findAllByItemOwnerIdAndBookingStatusOrderByStartDesc(Long ownerId, BookingStatus bookingStatus, PageRequest pageRequest);

    Page<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId, LocalDateTime start, LocalDateTime end, PageRequest pageRequest);

    Page<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime end, PageRequest pageRequest);

    Page<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime start, PageRequest pageRequest);

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
