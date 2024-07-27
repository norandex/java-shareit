package ru.practicum.shareit.item;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID) Long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemClient.create(userId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersItems(@RequestHeader(USER_ID) Long userId,
                                                @RequestParam(defaultValue = "0", required = false)
                                                @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10", required = false)
                                                @Positive Integer size) {
        return itemClient.getUsersItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(value = USER_ID, required = false) Long userId,
                                           @PathVariable Long itemId) {
        return itemClient.findById(userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> editItem(@RequestHeader(USER_ID) Long userId,
                                           @PathVariable long itemId, @RequestBody ItemDto itemDto) {
        return itemClient.editItem(userId, itemId, itemDto);
    }


    @GetMapping("/search")
    public ResponseEntity<Object> findByText(@RequestParam String text,
                                             @RequestParam(defaultValue = "0", required = false)
                                             @PositiveOrZero Integer from,
                                             @RequestParam(defaultValue = "10", required = false)
                                             @Positive Integer size) {
        return itemClient.findByText(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> comment(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId,
                                          @Valid @RequestBody CommentShortDto commentShortDto) {
        return itemClient.comment(userId, itemId, commentShortDto);
    }
}
