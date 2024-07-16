package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final HashMap<Long, User> repository = new HashMap<>();
    private long id = 1;

    @Override
    public User createUser(User user) {
        user.setId(id++);
        repository.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        return repository.get(id);
    }

    @Override
    public User deleteUserById(Long id) {
        return repository.remove(id);
    }

    @Override
    public List<User> getUsers() {
        return repository.values().stream().toList();
    }

    @Override
    public User updateUser(Long id, User user) {
        user.setId(id);
        repository.put(user.getId(), user);
        return user;
    }

    public boolean isEmailExists(String email) {
        return repository.values()
                .stream()
                .map(User::getEmail)
                .anyMatch(o -> o.contains(email));
    }
}
