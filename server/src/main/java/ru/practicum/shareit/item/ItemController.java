package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemCreateRequest request) {
        log.info("Запрос на создание вещи от пользователя {}", userId);
        return itemService.createItem(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemUpdateRequest request) {
        log.info("Запрос на обновление вещи {} от пользователя {}", itemId, userId);
        return itemService.updateItem(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ItemExtendedDto get(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long itemId) {
        log.info("Запрос вещи {} для пользователя {}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemExtendedDto> getOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос всех вещей пользователя {}", userId);
        return itemService.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Поиск вещей по тексту: {}", text);
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        log.info("Запрос на добавление комментария к вещи {} от пользователя {}", itemId, userId);
        return itemService.addComment(userId, itemId, commentDto);
    }
}
