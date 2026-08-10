package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemRequestCreateDto dto) {
        log.info("Запрос на создание запроса от пользователя {}", userId);
        return requestService.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос своих запросов для пользователя {}", userId);
        return requestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOther(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос чужих запросов для пользователя {} (from={}, size={})", userId, from, size);
        return requestService.getOtherRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long requestId) {
        log.info("Запрос запроса {} для пользователя {}", requestId, userId);
        return requestService.getRequestById(userId, requestId);
    }
}
