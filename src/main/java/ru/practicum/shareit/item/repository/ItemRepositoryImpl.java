package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ItemRepositoryImpl implements ItemRepository {

    private final HashMap<Long, Item> repository = new HashMap<>();
    private long id = 1;

    @Override
    public Item createItem(Item item) {
        item.setId(id++);
        return repository.put(item.getId(), item);
    }

    @Override
    public Item updateItem(Item item) {
        return repository.put(item.getId(), item);
    }

    @Override
    public Item deleteItem(Long itemId) {
        return repository.remove(itemId);
    }

    @Override
    public List<Item> getItems() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Item getItem(Long itemId) {
        return repository.get(itemId);
    }
}
