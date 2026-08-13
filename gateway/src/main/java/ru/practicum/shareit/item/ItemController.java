package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

import static ru.practicum.shareit.constant.Headers.USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID) Long userId,
                                         @Valid @RequestBody ItemCreateRequest request) {
        log.info("Создание вещи для пользователя {}: {}", userId, request);
        return itemClient.createItem(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemUpdateRequest request) {
        log.info("Обновление вещи {} для пользователя {}", itemId, userId);
        return itemClient.updateItem(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader(USER_ID) Long userId,
                                      @PathVariable Long itemId) {
        log.info("Получение вещи {} для пользователя {}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(USER_ID) Long userId) {
        log.info("Получение всех вещей пользователя {}", userId);
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Поиск вещей по тексту: {}", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        log.info("Добавление комментария к вещи {} от пользователя {}", itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
