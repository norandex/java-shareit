package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.InvalidUserException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Mapper.CommentMapper;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("create item");
        log.info(itemDto.toString());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));
        if (itemDto.getRequestId() != null) {
            log.info("not implemented");
//            not implemented
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("edit item");
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("item id " + itemId + " not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));
        if (!user.getId().equals(item.getOwner().getId())) {
            throw new InvalidUserException("Invalid user exception");
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        log.info("get items of user");
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));

        List<Item> foundItems = itemRepository.findAllByOwnerId(userId);
        if (foundItems.isEmpty()) {
            return null;
        }
        Collection<Booking> lastBookings = bookingRepository
                .findAllByItemOwnerIdAndBookingStatusIsAndStartBefore(userId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now(),
                        Sort.by("start").descending());
        Collection<Booking> nextBookings = bookingRepository
                .findAllByItemOwnerIdAndBookingStatusIsAndStartAfter(userId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now(),
                        Sort.by("start").ascending());

        Collection<ItemDto> finalItems = new ArrayList<>();

        Collection<Comment> comments = commentRepository.findAllByItemOwnerId(userId);

        for (Item item : foundItems) {
            List<CommentDto> commentsDto = comments.stream()
                    .filter(comment -> comment.getItem().getId().equals(item.getId()))
                    .map(comment -> CommentMapper.toCommentDto(comment, comment.getAuthor().getName()))
                    .collect(Collectors.toList());

            Optional<Booking> lastBooking = lastBookings.stream()
                    .filter(booking -> booking.getItem().getId().equals(item.getId()))
                    .findFirst();

            Optional<Booking> nextBooking = nextBookings.stream()
                    .filter(booking -> booking.getItem().getId().equals(item.getId()))
                    .findFirst();

            ItemDto itemDto = ItemMapper.toItemDto(item);

            itemDto.setComments(commentsDto);

            lastBooking.ifPresent(booking -> itemDto.setLastBooking(BookingMapper
                    .toBookingReservationDto(booking)));

            nextBooking.ifPresent(booking -> itemDto.setNextBooking(BookingMapper
                    .toBookingReservationDto(booking)));

            finalItems.add(itemDto);
        }
        return new ArrayList<>(finalItems);
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("item id " + itemId + " not found"));

        if (item.getOwner().getId().equals(userId)) {
            Collection<Booking> lastBookings = bookingRepository
                    .findAllByItemOwnerIdAndBookingStatusIsAndStartBefore(userId,
                            BookingStatus.APPROVED,
                            LocalDateTime.now(),
                            Sort.by("start").descending());
            Collection<Booking> nextBookings = bookingRepository
                    .findAllByItemOwnerIdAndBookingStatusIsAndStartAfter(userId,
                            BookingStatus.APPROVED,
                            LocalDateTime.now(),
                            Sort.by("start").ascending());


            ItemDto itemDto = ItemMapper.toItemDto(item);

            Optional<Booking> lastBooking = lastBookings.stream()
                    .filter(booking -> booking.getItem().getId().equals(item.getId()))
                    .findFirst();

            Optional<Booking> nextBooking = nextBookings.stream()
                    .filter(booking -> booking.getItem().getId().equals(item.getId()))
                    .findFirst();

            lastBooking.ifPresent(booking -> itemDto.setLastBooking(BookingMapper
                    .toBookingReservationDto(booking)));

            nextBooking.ifPresent(booking -> itemDto.setNextBooking(BookingMapper
                    .toBookingReservationDto(booking)));

            List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());

            itemDto.setComments(comments.stream().map(comment ->
                    CommentMapper.toCommentDto(comment, comment.getAuthor().getName())).collect(Collectors.toList()));

            return itemDto;
        }
        ItemDto itemDto = ItemMapper.toItemDto(item);
        List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());
        itemDto.setComments(comments.stream().map(comment ->
                CommentMapper.toCommentDto(comment, comment.getAuthor().getName())).collect(Collectors.toList()));

        return itemDto;
    }

    @Override
    public List<ItemDto> findByText(String text) {
        log.info("find item by text");
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> foundItems = itemRepository.searchByText(text);
        if (foundItems.isEmpty()) {
            return null;
        }
        return foundItems.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
