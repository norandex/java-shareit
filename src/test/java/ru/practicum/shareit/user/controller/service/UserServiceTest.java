package ru.practicum.shareit.user.controller.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.EmailCollisionException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUserTest() {
        User user = User.builder().name("user").email("test@mail.com").build();
        when(userRepository.save(Mockito.any())).thenReturn(user);
        UserDto userDto = UserDto.builder().name(user.getName()).email(user.getEmail()).build();
        UserDto savedUserDto = userService.createUser(userDto);
        assertNotNull(userDto);
        assertEquals(user.getName(), savedUserDto.getName());
        assertEquals(user.getEmail(), savedUserDto.getEmail());
    }

    @Test
    void updateUserTest() {
        UserDto userDto = UserDto.builder().name("updated user").email("new@mail.ru").build();

        User user = User.builder().id(1L).name("user").email("test@mail.ru").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userRepository.save(any())).thenReturn(user);


        userDto = userService.updateUser(1L, userDto);

        assertNotNull(userDto);
        assertEquals(userDto.getEmail(), user.getEmail());
        assertEquals(userDto.getName(), user.getName());
    }

    @Test
    void updateThrowsUserNotFoundException() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("test name")
                .email("test@mail.ru")
                .build();
        when(userRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException =
                assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, userDto));
        assertEquals(userNotFoundException.getMessage(), "user not found");
    }

    @Test
    void getUserTest() {
        User user = User.builder().id(1L).name("test name").email("test@mail.ru").build();

        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));


        UserDto userDto = userService.getUser(1L);
        assertNotNull(userDto);
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void getUsersTest() {
        User user1 = User.builder().id(1L).name("test user 1").email("test1@mail.ru").build();

        User user2 = User.builder().id(2L).name("test user 2").email("test2@mail.ru").build();
        List<User> userList = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(userList);

        Collection<UserDto> userDtos = userService.getUsers();

        assertNotNull(userDtos);
        assertIterableEquals(userDtos.stream().map(UserDto::getName).collect(Collectors.toList()), userList.stream().map(User::getName).collect(Collectors.toList()));
    }

    @Test
    void findByIdThrowsUserNotFoundExceptionTest() {
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> userService.getUser(100L));
        assertEquals(userNotFoundException.getMessage(), "no user 100 found");
    }

    @Test
    void deleteUserTest() {
        User user = User.builder().id(1L).name("user").email("test@mail.ru").build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userRepository.deleteById(user.getId());
        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void updateUserThrowsEmailCollisionExceptionTest() {
        UserDto userDto = UserDto.builder()
                .name("test name")
                .email("test@mail.ru")
                .build();
        User user = User.builder()
                .name("another name")
                .email("test@mail.ru")
                .build();
        assertEquals(user.getEmail(), userDto.getEmail());
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail(anyString()))
                .thenThrow(new EmailCollisionException("email collision"));
        EmailCollisionException emailCollisionException = assertThrows(EmailCollisionException.class,
                () -> userService.updateUser(1L, userDto));
        assertEquals(emailCollisionException.getMessage(), "email collision");

    }

    @Test
    void deleteUserThrowsUserNotFoundExceptionTest() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(1L));
        assertEquals(userNotFoundException.getMessage(), "no user 1 found");
    }
}
