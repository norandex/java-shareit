package ru.practicum.shareit.items.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ItemServiceTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @BeforeEach
    public void setUp() {
        itemService = new ItemServiceImpl(userRepository, itemRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void createItemTest() {
        User owner = User.builder().id(1L).name("user").email("user@mail.ru").build();
        ItemDto item = ItemDto.builder().name("item").description("info").available(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        ItemDto savedItem = itemService.createItem(1L, item);

        assertNotNull(item);
        assertEquals(item.getName(), savedItem.getName());
        assertEquals(item.getDescription(), savedItem.getDescription());
        assertEquals(item.getAvailable(), savedItem.getAvailable());
    }

    @Test
    void createItemThrowsUserNotFoundExceptionTest() {
        ItemDto item = ItemDto.builder().name("item").description("info").available(true).build();

        when(userRepository.findById(any())).thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> itemService.createItem(1L, item));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void createItemThrowsRequestNotFoundExceptionTest() {
        User user = User.builder().id(1L).name("owner").email("owner@mail.ru").build();
        ItemDto itemToSave = ItemDto.builder().name("item").description("info").available(true).requestId(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(any())).thenReturn(Optional.empty());

        ItemRequestNotFoundException itemRequestNotFoundException = assertThrows(ItemRequestNotFoundException.class, () -> itemService.createItem(1L, itemToSave));
        assertEquals(itemRequestNotFoundException.getMessage(), "item request not found");
    }

    @Test
    void updateItemTest() {
        User user = User.builder().id(1L).name("user").email("user@mail.ru").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ItemDto itemDto = ItemDto.builder().name("name updated").description("info updated").build();

        Item item = Item.builder().id(1L).name("item name").description("info").available(true).owner(user).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item itemUpdated = Item.builder().id(1L).name(itemDto.getName())
                .description(itemDto.getDescription()).available(true).owner(user).build();

        when(itemRepository.save(any())).thenReturn(itemUpdated);

        itemDto = itemService.updateItem(1L, 1L, itemDto);
        assertNotNull(itemDto);
        assertEquals(item.getName(), itemUpdated.getName());
        assertEquals(item.getDescription(), itemUpdated.getDescription());
        assertEquals(item.getAvailable(), itemUpdated.getAvailable());
    }

    @Test
    void updateItemThrowsUserNotFoundExceptionTest() {

        ItemDto item = ItemDto.builder().name("item").description("info").available(true).build();

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, item));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void updateItemThrowsItemNotFoundExceptionTest() {
        User user = User.builder().id(1L).name("user").email("user@mail.ru").build();
        ItemDto itemToSave = ItemDto.builder().name("item").description("info").available(true).requestId(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(any())).thenReturn(Optional.empty());

        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, itemToSave));
        assertEquals(itemNotFoundException.getMessage(), "item id 1 not found");
    }

    @Test
    void updateTestFailUserThrowsIncorrectUserOperationException() {
        User user = User.builder().id(1L).name("thief").email("test@mail.ru").build();

        User owner = User.builder().id(2L).name("user").email("user@mail.ru").build();

        Item item = Item.builder().id(1L).name("item name").description("description").owner(owner).available(true).build();

        ItemDto itemDto = ItemDto.builder().name("updated name").description("updated description").available(true).build();

        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(userRepository.existsById(any())).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        InvalidUserException e = assertThrows(InvalidUserException.class, () -> itemService.updateItem(user.getId(), item.getId(), itemDto));

        assertEquals(e.getMessage(), "Invalid user exception");
    }

    @Test
    void getItemsTest() {
        User owner = User.builder().id(2L).name("user").email("owner@mail.ru").build();

        User booker = User.builder().id(3L).name("booker").email("booker@mail.ru").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(Page.empty());

        List<ItemDto> itemDtos = itemService.getItems(2L, 0, 11);
        Assertions.assertTrue(itemDtos.isEmpty());

        Item item = Item.builder().id(1L).name("item name").description("description").available(true).owner(owner).build();

        List<Item> items = new ArrayList<>();
        items.add(item);

        when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(new PageImpl<>(items));

        LocalDateTime created = LocalDateTime.now();

        Comment comment = Comment.builder().id(1L).text("text").item(item).author(booker).created(created).build();

        List<Comment> commentList = List.of(comment);

        when(commentRepository.findAllByItemId(1L)).thenReturn(commentList);

        Booking lastBooking = Booking.builder().id(1L).start(created.minusMonths(5)).end(created.minusMonths(4)).item(item).booker(booker).build();

        Booking nextBooking = Booking.builder().id(2L).start(created.plusYears(1L)).end(created.plusYears(2L)).item(item).booker(booker).build();

        when(bookingRepository.findAllByItemOwnerIdAndBookingStatusIsAndStartBefore(any(), any(), any(), any())).thenReturn(List.of(lastBooking));
        when(bookingRepository.findAllByItemOwnerIdAndBookingStatusIsAndStartAfter(any(), any(), any(), any())).thenReturn(List.of(nextBooking));

        itemDtos = itemService.getItems(2L, 0, 11);
        assertNotNull(itemDtos);

        Item item2 = Item.builder().id(2L).name("item 2 name").description("description 2").available(true).owner(owner).build();

        items.add(item2);
        when(commentRepository.findAllByItemId(2L)).thenReturn(Collections.emptyList());

        itemDtos = itemService.getItems(2L, 0, 11);
        assertNotNull(itemDtos);
    }

    @Test
    void getItemsThrowsUserNotFoundExceptionTest() {
        when(userRepository.existsById(any())).thenReturn(false);
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> itemService.getItems(1L, 0, 11));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void getUsersItemsTestThrowsPaginationException() {
        User user = User.builder().id(2L).name("user").email("user@mail.ru").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        IncorrectPaginationException incorrectPaginationException;

        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> itemService.getItems(2L, -1, 11));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");

        incorrectPaginationException = assertThrows(IncorrectPaginationException.class,
                () -> itemService.getItems(2L, 0, 0));
        assertEquals(incorrectPaginationException.getMessage(), "pagination error");
    }

    @Test
    void getItemTest() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        User owner = User.builder()
                .id(2L)
                .name("user 2")
                .email("user2@mail.ru")
                .build();

        User booker = User.builder()
                .id(3L)
                .name("user 3")
                .email("user3@mail.ru")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();

        LocalDateTime created = LocalDateTime.now();
        Comment comment = Comment.builder()
                .id(1L)
                .text("text")
                .item(item)
                .author(booker)
                .created(created)
                .build();
        List<Comment> commentList = List.of(comment);

        Booking lastBooking = Booking.builder()
                .id(1L)
                .start(created.minusDays(8))
                .end(created.minusDays(6))
                .item(item)
                .booker(booker)
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .start(created.plusDays(1L))
                .end(created.plusDays(2L))
                .item(item)
                .booker(booker)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(owner));

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(commentRepository.findAllByItemId(1L))
                .thenReturn(commentList);

        ItemDto itemDto = itemService.getItem(1L, 1L);
        assertNotNull(itemDto);

        when(bookingRepository.findAllByItemOwnerIdAndBookingStatusIsAndStartBefore(any(), any(), any(), any()))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findAllByItemOwnerIdAndBookingStatusIsAndStartAfter(any(), any(), any(), any()))
                .thenReturn(List.of(nextBooking));

        itemDto = itemService.getItem(2L, 1L);

        assertNotNull(itemDto);
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.getAvailable());
    }

}
