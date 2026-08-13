package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Запрос всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        log.info("Запрос пользователя с id={}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("Обновление пользователя id={}, данные={}", id, userDto);
        return userService.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удаление пользователя id={}", id);
        userService.deleteUser(id);
    }
}
