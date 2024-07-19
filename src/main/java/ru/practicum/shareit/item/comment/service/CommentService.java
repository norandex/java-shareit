package ru.practicum.shareit.item.comment.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentShortDto;

public interface CommentService {
    CommentDto comment(Long userId, Long itemId, CommentShortDto commentShortDto);
}
