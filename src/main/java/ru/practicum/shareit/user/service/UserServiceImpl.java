package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailCollisionException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepositoryImpl userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("create user");
        if (userRepository.isEmailExists(userDto.getEmail())) {
            throw new EmailCollisionException("email collision");
        }
        User user = UserMapper.toUser(userDto);
        userRepository.createUser(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("update user");
        User oldUser = userRepository.getUserById(id);
        if (oldUser == null) {
            throw new UserNotFoundException("no user with id + " + id);
        }

        if (userDto.getEmail() != null) {
            if (userRepository.isEmailExists(userDto.getEmail()) && !oldUser.getEmail().equals(userDto.getEmail())) {
                throw new EmailCollisionException("email collision");
            }
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        userRepository.updateUser(id, oldUser);
        return UserMapper.toUserDto(oldUser);
    }

    @Override
    public UserDto deleteUser(Long userId) {
        log.info("delete user");
        return UserMapper.toUserDto(userRepository.deleteUserById(userId));
    }

    @Override
    public List<UserDto> getUsers() {
        log.info("get users");
        return userRepository.getUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("get user");
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("no user " + userId + " found");
        }
        return UserMapper.toUserDto(userRepository.getUserById(userId));
    }
}
