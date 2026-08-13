package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void shouldMapToUserDto() {
        User user = new User(1L, "Иван", "ivan@mail.com");
        UserDto dto = UserMapper.toUserDto(user);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Иван");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void shouldMapToUser() {
        UserDto dto = new UserDto(1L, "Иван", "ivan@mail.com");
        User user = UserMapper.toUser(dto);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Иван");
        assertThat(user.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void shouldReturnNullWhenUserNull() {
        assertThat(UserMapper.toUserDto(null)).isNull();
        assertThat(UserMapper.toUser(null)).isNull();
    }
}
