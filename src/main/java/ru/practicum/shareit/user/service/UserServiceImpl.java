package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailCollisionException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("create user");
        User user = UserMapper.toUser(userDto);
        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("update user");
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found"));

        if (userDto.getEmail() != null) {
            if (userRepository.existsByEmail(userDto.getEmail()) && !oldUser.getEmail().equals(userDto.getEmail())) {
                throw new EmailCollisionException("email collision");
            }
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        User user = userRepository.save(oldUser);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto deleteUser(Long userId) {
        log.info("delete user");
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("no user " + userId + " found"));
        userRepository.delete(userToDelete);
        return UserMapper.toUserDto(userToDelete);
    }

    @Override
    public List<UserDto> getUsers() {
        log.info("get all users");
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("get user");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("no user " + userId + " found"));
        return UserMapper.toUserDto(user);
    }
}
