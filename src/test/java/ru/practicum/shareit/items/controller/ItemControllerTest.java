package ru.practicum.shareit.items.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentShortDto;
import ru.practicum.shareit.item.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CommentService commentService;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDtoCreateTest;

    private ItemDto itemDtoCreated;

    private ItemDto itemDtoUpdateTest;

    private ItemDto itemDtoUpdated;

    private CommentShortDto commentShortDto;

    private CommentDto commentDtoCreated;

    @BeforeEach
    public void prepare() {
        itemDtoCreateTest = ItemDto.builder()
                .name("test item name")
                .description("test description")
                .available(true)
                .build();

        itemDtoCreated = ItemDto.builder()
                .id(1L)
                .name("test item name")
                .description("test description")
                .available(true)
                .build();

        itemDtoUpdateTest = ItemDto.builder()
                .description("description to update test")
                .build();

        itemDtoUpdated = ItemDto.builder()
                .id(1L)
                .name("test item name")
                .description("description to update test")
                .available(true)
                .build();

        commentShortDto = CommentShortDto.builder()
                .text("test comment")
                .build();

        commentDtoCreated = CommentDto.builder()
                .id(1L)
                .text("test comment")
                .authorName("test author name")
                .created(LocalDateTime.now())
                .build();
    }

    @AfterEach
    public void clean() {
        itemDtoCreateTest = null;
        itemDtoCreated = null;
        itemDtoUpdateTest = null;
        itemDtoUpdated = null;
    }

    @Test
    public void createTest() throws Exception {
        when(itemService.createItem(1L, itemDtoCreateTest))
                .thenReturn(itemDtoCreated);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoCreateTest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoCreated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoCreated.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoCreated.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoCreated.getAvailable())));
    }

    @Test
    public void updateTest() throws Exception {
        when(itemService.updateItem(1L, 1L, itemDtoUpdateTest))
                .thenReturn(itemDtoUpdated);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDtoUpdateTest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoUpdated.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoUpdated.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoUpdated.getAvailable())));
    }

    @Test
    public void findByIdTest() throws Exception {
        when(itemService.getItem(1L, 1L))
                .thenReturn(itemDtoUpdated);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoUpdated.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoUpdated.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoUpdated.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoUpdated.getAvailable())));
    }

    @Test
    public void searchByTextTest() throws Exception {
        when(itemService.findByText("update", 0, 10))
                .thenReturn(List.of(itemDtoUpdated));

        mvc.perform(get("/items/search?text=update")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDtoUpdated.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoUpdated.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoUpdated.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoUpdated.getAvailable())));
    }

    @Test
    public void commentTest() throws Exception {
        when(commentService.comment(1L, 1L, commentShortDto))
                .thenReturn(commentDtoCreated);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentShortDto))
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDtoCreated.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDtoCreated.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDtoCreated.getAuthorName())))
                .andExpect(jsonPath("$.created",
                        is(commentDtoCreated.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))));
    }
}
