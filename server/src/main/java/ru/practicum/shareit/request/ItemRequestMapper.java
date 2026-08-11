package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestCreateDto dto, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        if (request.getItems() != null) {
            List<ItemForRequestDto> itemDtos = request.getItems().stream()
                    .map(item -> new ItemForRequestDto(item.getId(), item.getName(), item.getOwner().getId()))
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }
        return dto;
    }
}
