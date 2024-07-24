package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.exception.IncorrectPaginationException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    @Autowired
    private final ItemRequestServiceImpl itemRequestService;

    @MockBean
    private final ItemRequestRepository itemRequestRepository;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRepository itemRepository;

    @Test
    void createItemRequestTest() {
        User requester = User.builder()
                .id(1L)
                .name("test name")
                .email("test@mail.ru")
                .build();

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(requester));

        ItemRequestShortDto itemRequestShortDto = ItemRequestShortDto.builder()
                .description("request info")
                .build();

        LocalDateTime now = LocalDateTime.now();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("item info")
                .requester(requester)
                .created(now)
                .build();

        when(itemRequestRepository.save(any()))
                .thenReturn(request);

        ItemRequestDto itemRequestDtoCreated = itemRequestService.createItemRequest(2L, itemRequestShortDto);
        assertNotNull(itemRequestDtoCreated);
        assertEquals(itemRequestDtoCreated.getDescription(), itemRequestShortDto.getDescription());
    }

    @Test
    void createThrowsUserNotFoundException() {
        ItemRequestShortDto itemRequestShortDto = ItemRequestShortDto.builder()
                .description("description")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.createItemRequest(1L, itemRequestShortDto));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void getItemRequestTest() {
        User user = User.builder()
                .id(1L)
                .name("user1")
                .email("test@mail.ru")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(new ArrayList<>());

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getItemRequests(1L);

        assertTrue(itemRequestDtos.isEmpty());

        LocalDateTime requestCreationDate = LocalDateTime.now();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(user)
                .created(requestCreationDate)
                .build();

        List<ItemRequest> itemRequests = List.of(request);

        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(1L))
                .thenReturn(itemRequests);

        User owner = User.builder()
                .id(2L)
                .name("user2")
                .email("user@mail.ru")
                .build();

        List<Item> items = Collections.emptyList();

        when(itemRepository.findAllByRequestIdIn(List.of(2L)))
                .thenReturn(items);

        itemRequestDtos = itemRequestService.getItemRequests(1L);

        assertTrue(itemRequestDtos.get(0).getItems().isEmpty());

        Item item = Item.builder()
                .id(1L)
                .name("test item name")
                .description("info")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        items = List.of(item);

        when(itemRepository.findAllByRequestIdIn(List.of(2L)))
                .thenReturn(items);

        itemRequestDtos = itemRequestService.getItemRequests(1L);

        assertNotNull(itemRequestDtos);
    }

    @Test
    void getItemRequestTestThrowsUserNotFoundException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.getItemRequests(1L));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void getAllItemRequestsTest() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        LocalDateTime now = LocalDateTime.now();

        User requester = User.builder()
                .id(2L)
                .name("name")
                .email("test@mail.ru")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requester(requester)
                .created(now)
                .build();

        List<ItemRequest> itemRequests = new ArrayList<>();

        when(itemRequestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(any(), any()))
                .thenReturn(new PageImpl<>(itemRequests));
        List<ItemRequestDto> itemRequestDtos = itemRequestService.getAllItemRequests(1L, 0, 11);
        assertTrue(itemRequestDtos.isEmpty());

        itemRequests = List.of(request);
        when(itemRequestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(any(), any()))
                .thenReturn(new PageImpl<>(itemRequests));

        List<Item> items = Collections.emptyList();
        when(itemRepository.findAllByRequestIdIn(List.of(1L)))
                .thenReturn(items);

        itemRequestDtos = itemRequestService.getAllItemRequests(1L, 0, 11);
        assertTrue(itemRequestDtos.get(0).getItems().isEmpty());

        Item item = Item.builder()
                .id(1L)
                .name("test name")
                .description("info")
                .available(true)
                .owner(user)
                .request(request)
                .build();
        items = List.of(item);

        when(itemRepository.findAllByRequestIdIn(List.of(1L)))
                .thenReturn(items);

        itemRequestDtos = itemRequestService.getAllItemRequests(1L, 0, 11);
        assertNotNull(itemRequestDtos);
    }

    @Test
    void getAllItemRequestsThrowsUserNotFoundException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.getAllItemRequests(1L, 0, 11));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void getAllItemRequestsThrowsIncorrectPaginationException() {
        User user = User.builder()
                .id(1L)
                .name("user")
                .email("user@mail.ru")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));


        IncorrectPaginationException invalidPageParamsException = assertThrows(IncorrectPaginationException.class,
                () -> itemRequestService.getAllItemRequests(1L, -1, 11));
        assertEquals(invalidPageParamsException.getMessage(), "incorrect pagination");

        invalidPageParamsException = assertThrows(IncorrectPaginationException.class,
                () -> itemRequestService.getAllItemRequests(1L, 1, 0));
        assertEquals(invalidPageParamsException.getMessage(), "incorrect pagination");
    }

    @Test
    void getItemRequestByIdTest() {
        User user = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        LocalDateTime now = LocalDateTime.now();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("info")
                .requester(user)
                .created(now)
                .build();

        when(itemRequestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));

        List<Item> items = Collections.emptyList();
        when(itemRepository.findAllByRequestId(1L))
                .thenReturn(items);

        ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(1L, 1L);
        assertTrue(itemRequestDto.getItems().isEmpty());

        Item item = Item.builder()
                .id(1L)
                .name("user")
                .description("description")
                .available(true)
                .owner(user)
                .request(request)
                .build();
        items = List.of(item);

        when(itemRepository.findAllByRequestId(1L))
                .thenReturn(items);

        itemRequestDto = itemRequestService.getItemRequestById(1L, 1L);

        assertNotNull(itemRequestDto);
    }

    @Test
    void getItemRequestByIdThrowsUserNotFoundException() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.getItemRequestById(1L, 1L));
        assertEquals(userNotFoundException.getMessage(), "user id 1 not found");
    }

    @Test
    void getItemRequestByIdThrowsItemRequestNotFoundException() {
        User user = User.builder()
                .id(1L)
                .name("test name")
                .email("test@mail.ru")
                .build();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        ItemRequestNotFoundException itemRequestNotFoundException = assertThrows(ItemRequestNotFoundException.class,
                () -> itemRequestService.getItemRequestById(1L, 1L));
        assertEquals(itemRequestNotFoundException.getMessage(), "item request id 1 not found");
    }


}
