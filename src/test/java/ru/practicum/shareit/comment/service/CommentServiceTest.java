package ru.practicum.shareit.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentShortDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.comment.service.CommentServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CommentServiceTest {

    @Autowired
    private CommentServiceImpl commentService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;

    @BeforeEach
    private void setUp() {
        commentService = new CommentServiceImpl(userRepository, itemRepository, commentRepository, bookingRepository);
    }

    @Test
    protected void commentTest() {

        User user1 = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("user 2")
                .email("user2@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user2)
                .build();


        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .text("info")
                .author(user1)
                .created(now)
                .item(item)
                .build();

        CommentShortDto commentShortDto = CommentShortDto.builder()
                .text(comment.getText())
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(now.minusDays(1L))
                .end(now.minusDays(2L))
                .item(item)
                .booker(user1)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findTopByItemIdAndBookerIdAndEndIsBeforeAndBookingStatusIs(any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(booking));

        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto savedCommentDto = commentService.comment(user1.getId(), item.getId(), commentShortDto);

        assertNotNull(savedCommentDto);
        assertEquals(comment.getAuthor().getName(), savedCommentDto.getAuthorName());
        assertEquals(comment.getText(), savedCommentDto.getText());
        assertEquals(comment.getCreated(), savedCommentDto.getCreated());
    }

    @Test
    protected void commentThrowsUserNotFoundException() {
        User user1 = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("user 2")
                .email("user2@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user2)
                .build();


        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .text("info")
                .author(user1)
                .created(now)
                .item(item)
                .build();

        CommentShortDto commentShortDto = CommentShortDto.builder()
                .text(comment.getText())
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(now.minusDays(1L))
                .end(now.minusDays(2L))
                .item(item)
                .booker(user1)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findTopByItemIdAndBookerIdAndEndIsBeforeAndBookingStatusIs(any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(booking));

        when(commentRepository.save(any())).thenReturn(comment);

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> commentService.comment(user1.getId(), item.getId(), commentShortDto));
        assertEquals(userNotFoundException.getMessage(), "user with id 1 not found");
    }

    @Test
    protected void commentThrowsItemNotFoundException() {
        User user1 = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("user 2")
                .email("user2@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user2)
                .build();


        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .text("info")
                .author(user1)
                .created(now)
                .item(item)
                .build();

        CommentShortDto commentShortDto = CommentShortDto.builder()
                .text(comment.getText())
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(now.minusDays(1L))
                .end(now.minusDays(2L))
                .item(item)
                .booker(user1)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        when(bookingRepository.findTopByItemIdAndBookerIdAndEndIsBeforeAndBookingStatusIs(any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(booking));

        when(commentRepository.save(any())).thenReturn(comment);

        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> commentService.comment(user1.getId(), item.getId(), commentShortDto));
        assertEquals(itemNotFoundException.getMessage(), "item with id 1 not found");
    }

    @Test
    protected void commentThrowsNotAvailableException() {
        User user1 = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("user 2")
                .email("user2@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item1")
                .description("info")
                .available(true)
                .owner(user2)
                .build();


        LocalDateTime now = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(1L)
                .text("info")
                .author(user1)
                .created(now)
                .item(item)
                .build();

        CommentShortDto commentShortDto = CommentShortDto.builder()
                .text(comment.getText())
                .build();

        Booking booking = Booking.builder()
                .id(2L)
                .start(now.minusDays(1L))
                .end(now.minusDays(2L))
                .item(item)
                .booker(user1)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findTopByItemIdAndBookerIdAndEndIsBeforeAndBookingStatusIs(any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        when(commentRepository.save(any())).thenReturn(comment);

        NotAvailableException notAvailableException = assertThrows(NotAvailableException.class,
                () -> commentService.comment(user1.getId(), item.getId(), commentShortDto));
        assertEquals(notAvailableException.getMessage(), "no booking found");
    }
}
