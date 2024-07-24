package ru.practicum.shareit.items.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        User owner = User.builder()
                .name("owner")
                .email("owner@email.com")
                .build();
        userRepository.save(owner);

        Item item = Item.builder()
                .name("item1")
                .description("item1 desc")
                .available(true)
                .owner(owner)
                .build();

        User booker = User.builder()
                .name("booker")
                .email("booker@email.com")
                .build();

        LocalDateTime created = LocalDateTime.now();

        Booking lastBooking = Booking.builder()
                .start(created.minusMonths(5))
                .end(created.minusMonths(4))
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.APPROVED)
                .build();

        Booking nextBooking = Booking.builder()
                .start(created.plusDays(1L))
                .end(created.plusDays(2L))
                .item(item)
                .booker(booker)
                .bookingStatus(BookingStatus.WAITING)
                .build();

        userRepository.save(owner);
        userRepository.save(booker);
        itemRepository.save(item);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);

    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DirtiesContext
    @Test
    void create() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        itemDto = itemService.createItem(1L, itemDto);

        assertNotNull(itemDto);
        assertEquals(itemDto.getId(), 2L);
    }

    @DirtiesContext
    @Test
    void getUserItems() throws Exception {
        List<ItemDto> itemDtos = itemService.getItems(1L, 0, 10);
        assertEquals(itemDtos.size(), 1);
        assertEquals(itemDtos.get(0).getName(), "item1");
    }
}
