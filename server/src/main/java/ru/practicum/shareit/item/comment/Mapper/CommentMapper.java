package ru.practicum.shareit.item.comment.Mapper;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentShortDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static Comment toComment(CommentShortDto commentShortDto, Item item, User user, LocalDateTime created) {
        return Comment.builder()
                .text(commentShortDto.getText())
                .item(item)
                .author(user)
                .created(created)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment, String author) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(author)
                .created(comment.getCreated())
                .build();
    }

}
