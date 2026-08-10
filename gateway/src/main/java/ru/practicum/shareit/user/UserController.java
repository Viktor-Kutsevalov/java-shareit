package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Запрос всех пользователей");
        return userClient.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        log.info("Запрос пользователя с id={}", id);
        return userClient.getUser(id);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("Обновление пользователя id={}, данные={}", id, userDto);
        return userClient.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("Удаление пользователя id={}", id);
        return userClient.deleteUser(id);
    }
}
