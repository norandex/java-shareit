package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestMapperTest {

    @Test
    protected void toItemRequestDtoTest() {
        User user = User.builder()
                .id(1L)
                .name("user name")
                .email("user@mail.ru")
                .build();

        UserDto userDto = UserMapper.toUserDto(user);
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description")
                .created(LocalDateTime.now())
                .requester(user)
                .build();

        ItemRequestDto itemRequestDto = ItemRequestMapper
                .toItemRequestDto(itemRequest, userDto);

        assertEquals(itemRequestDto.getRequester().getName(), itemRequest.getRequester().getName());
        assertEquals(itemRequestDto.getRequester().getEmail(), itemRequest.getRequester().getEmail());
    }

    @Test
    protected void toItemRequestTest() {
        ItemRequestShortDto itemRequestShortDto = ItemRequestShortDto.builder()
                .description("description")
                .build();
        User user = User.builder()
                .id(1L)
                .name("user name")
                .email("user@mail.ru")
                .build();
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestShortDto, user, LocalDateTime.now());
        assertEquals(itemRequest.getDescription(), itemRequestShortDto.getDescription());
    }
}
