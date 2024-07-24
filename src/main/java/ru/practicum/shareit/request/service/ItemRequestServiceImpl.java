package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmptyDescriptionException;
import ru.practicum.shareit.exception.IncorrectPaginationException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestShortDto itemRequestShortDto) {
        log.info("create item request");
        if (itemRequestShortDto.getDescription() == null || itemRequestShortDto.getDescription().isEmpty()) {
            throw new EmptyDescriptionException("empty description");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestShortDto, user, LocalDateTime.now());
        UserDto userDto = UserMapper.toUserDto(user);
        itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest, userDto);
    }

    @Override
    public List<ItemRequestDto> getItemRequests(Long userId) {
        log.info("get item request of current user");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user.getId());
        if (itemRequests.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.findAllByRequestIdIn(itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList()));
        UserDto userDto = UserMapper.toUserDto(user);
        List<ItemRequestDto> itemRequestDtos = itemRequests.stream()
                .map(o -> ItemRequestMapper.toItemRequestDto(o, userDto))
                .collect(Collectors.toList());
        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            itemRequestDto.setItems(items.stream()
                    .filter(item -> item.getRequest().getId().equals(itemRequestDto.getId()))
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList()));
        }
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        log.info("get item request of current user");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));
        if (from < 0 || size < 1) {
            throw new IncorrectPaginationException("incorrect pagination");
        }
        List<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequesterIdIsNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size)).getContent();
        if (itemRequests.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.findAllByRequestIdIn(itemRequests
                .stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList()));
        List<ItemRequestDto> itemRequestDtos = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            List<ItemDto> itemDtos = items.stream()
                    .filter(item -> item.getRequest().getId().equals(itemRequest.getId()))
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
            UserDto userDto = UserMapper.toUserDto(itemRequest.getRequester());
            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest, userDto);
            itemRequestDto.setItems(itemDtos);
            itemRequestDtos.add(itemRequestDto);
        }
        return itemRequestDtos;
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long itemRequestId) {
        log.info("get item request by id");
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user id " + userId + " not found"));
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("item request id " + itemRequestId + " not found"));
        List<ItemDto> items = itemRepository.findAllByRequestId(itemRequest.getId()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest,
                UserMapper.toUserDto(itemRequest.getRequester()));
        itemRequestDto.setItems(items);
        return itemRequestDto;
    }
}
