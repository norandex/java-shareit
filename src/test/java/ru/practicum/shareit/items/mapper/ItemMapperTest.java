package ru.practicum.shareit.items.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ItemMapperTest {

    @Test
    protected void toItemTest() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item name")
                .description("description")
                .available(true)
                .build();

        Item item = ItemMapper.toItem(itemDto);
        assertEquals(item.getName(), itemDto.getName());
    }
}
