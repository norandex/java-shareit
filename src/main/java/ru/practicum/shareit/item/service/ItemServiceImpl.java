package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InvalidUserException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepositoryImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserRepositoryImpl userRepository;
    private final ItemRepositoryImpl itemRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("create item");
        log.info(itemDto.toString());
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user id " + userId + " not found");
        }
        itemDto.setOwner(user);
        Item item = ItemMapper.toItem(itemDto);
        itemRepository.createItem(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto item) {
        return null;
    }

    @Override
    public ItemDto deleteItem(Long userId, Long itemId) {
        Item item = itemRepository.getItem(itemId);
        return null;
    }

    @Override
    public ItemDto editItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("edit item");
        Item item = itemRepository.getItem(itemId);
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user id " + userId + " not found");
        }
        if (item == null) {
            throw new ItemNotFoundException("item id " + itemId + " not found");
        }
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

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        log.info("get items of user");
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("user id " + userId + " not found");
        }
        List<Item> foundItems = itemRepository.getItems().stream()
                .filter(o -> o.getOwner().getId().equals(userId))
                .toList();
        if (foundItems.isEmpty()) {
            return null;
        }
        return foundItems.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.getItem(itemId));
    }

    @Override
    public List<ItemDto> findByText(String text) {
        log.info("find item by text");
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> foundItems = itemRepository.getItems().stream()
                .filter(Item::isAvailable)
                .filter(o -> o.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
        if (foundItems.isEmpty()) {
            return null;
        }
        return foundItems.stream().map(ItemMapper::toItemDto).toList();
    }
}
