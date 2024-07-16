package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto item);

    ItemDto updateItem(Long userId, ItemDto item);

    ItemDto deleteItem(Long userId, Long itemId);

    ItemDto editItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> getItems(Long userId);

    ItemDto getItem(Long itemId);

    List<ItemDto> findByText(String text);
}
