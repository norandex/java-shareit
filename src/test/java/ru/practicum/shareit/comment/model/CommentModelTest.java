package ru.practicum.shareit.comment.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comment.model.Comment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentModelTest {

    @Test
    protected void testEquals() {
        Comment comment1 = Comment.builder()
                .id(1L)
                .build();

        Comment comment2 = Comment.builder()
                .id(1L)
                .build();

        Comment comment3 = Comment.builder()
                .id(null)
                .build();

        assertTrue(comment1.equals(comment2));
        assertTrue(comment1.equals(comment1));
        assertFalse(comment1.equals(1L));
        assertFalse(comment1.equals(comment3));
        assertFalse(comment3.equals(comment1));
    }
}
