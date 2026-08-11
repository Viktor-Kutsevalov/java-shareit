package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static ru.practicum.shareit.constant.Headers.USER_ID;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID) Long userId,
                                         @Valid @RequestBody ItemRequestCreateDto dto) {
        log.info("Создание запроса для пользователя {}: {}", userId, dto);
        return requestClient.createRequest(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(USER_ID) Long userId) {
        log.info("Получение своих запросов для пользователя {}", userId);
        return requestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getOther(@RequestHeader(USER_ID) Long userId,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                           @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение чужих запросов для пользователя {}, from={}, size={}", userId, from, size);
        return requestClient.getOtherRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID) Long userId,
                                          @PathVariable Long requestId) {
        log.info("Получение запроса {} для пользователя {}", requestId, userId);
        return requestClient.getRequestById(userId, requestId);
    }
}
