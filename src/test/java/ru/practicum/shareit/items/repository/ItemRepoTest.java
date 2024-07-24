package ru.practicum.shareit.items.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepoTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByText() {
        User user = User.builder()
                .id(1L)
                .name("test")
                .email("test@mail.ru")
                .build();

        Item item = Item.builder()
                .name("item 1")
                .description("info")
                .available(true)
                .owner(user)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 11);
        userRepository.save(user);
        itemRepository.save(item);

        List<Item> itemList = itemRepository.searchByText("inF", pageRequest).toList();

        assertNotNull(itemList);
        Item itemFound = itemList.get(0);
        assertEquals(item.getName(), itemFound.getName());
        assertEquals(item.getDescription(), itemFound.getDescription());

        List<Item> anotherItemList = itemRepository.searchByText("123", pageRequest).toList();
        assertEquals(anotherItemList.size(), 0);

        userRepository.deleteAll();
        itemRepository.deleteAll();
    }
}
