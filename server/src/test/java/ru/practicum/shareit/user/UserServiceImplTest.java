package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User(1L, "Иван", "ivan@mail.com"),
                new User(2L, "Петр", "petr@mail.com")
        ));
        List<UserDto> dtos = userService.getAllUsers();
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getName()).isEqualTo("Иван");
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "Иван", "ivan@mail.com")));
        UserDto dto = userService.getUserById(1L);
        assertThat(dto.getName()).isEqualTo("Иван");
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldCreateUser() {
        UserDto dto = new UserDto(null, "Иван", "ivan@mail.com");
        when(userRepository.existsByEmail("ivan@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "Иван", "ivan@mail.com"));

        UserDto saved = userService.createUser(dto);
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void shouldThrowConflictWhenEmailExists() {
        UserDto dto = new UserDto(null, "Иван", "ivan@mail.com");
        when(userRepository.existsByEmail("ivan@mail.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldUpdateUser() {
        User existing = new User(1L, "Иван", "ivan@mail.com");
        UserDto dto = new UserDto(null, "Петр", "petr@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("petr@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "Петр", "petr@mail.com"));

        UserDto updated = userService.updateUser(1L, dto);
        assertThat(updated.getName()).isEqualTo("Петр");
        assertThat(updated.getEmail()).isEqualTo("petr@mail.com");
    }

    @Test
    void shouldDeleteUser() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}
