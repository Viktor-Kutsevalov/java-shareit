package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestCreateDto dto);

    List<ItemRequestDto> getOwnRequests(Long userId);

    List<ItemRequestDto> getOtherRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
