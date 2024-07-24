package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto item);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> getItems(Long userId);

    ItemDto getItem(Long itemId, Long userId);

    List<ItemDto> findByText(String text);
}
