package ru.practicum.shareit.item.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotAllowedActionException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Mapper.CommentMapper;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentShortDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    @Override
    public CommentDto comment(Long userId, Long itemId, CommentShortDto commentShortDto) {
        log.info("comment");
        if (commentShortDto.getText().isBlank()) {
            throw new NotAllowedActionException("can't leave a blank comment");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id " + userId + " not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("item with id " + itemId + " not found"));

        bookingRepository.findTopByItemIdAndBookerIdAndEndIsBeforeAndBookingStatusIs(itemId,
                        userId, LocalDateTime.now(), BookingStatus.APPROVED,
                        Sort.by(Sort.Direction.DESC, "end"))
                .orElseThrow(() -> new NotAvailableException("no booking found"));

        Comment comment = commentRepository.save(CommentMapper.toComment(commentShortDto, item, user, LocalDateTime.now()));

        return CommentMapper.toCommentDto(comment, user.getName());
    }
}
