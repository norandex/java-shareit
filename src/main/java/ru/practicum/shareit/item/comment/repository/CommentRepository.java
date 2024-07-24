package ru.practicum.shareit.item.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.comment.model.Comment;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(Long itemId);

    List<Comment> findAllByItemOwnerId(Long ownerId);
}
