package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentShortDto;
import ru.practicum.shareit.item.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID) Long userId, @RequestBody @Valid ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto editItem(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(value = USER_ID, required = false) Long userId,
                           @PathVariable Long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(USER_ID) Long userId) {
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findByText(@RequestParam String text) {
        return itemService.findByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto comment(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId,
                              @RequestBody CommentShortDto commentShortDto) {
        return commentService.comment(userId, itemId, commentShortDto);
    }
}
