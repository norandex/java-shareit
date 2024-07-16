package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    User createUser(User user);

    User getUserById(Long id);

    User deleteUserById(Long id);

    List<User> getUsers();

    User updateUser(Long id, User user);

}
