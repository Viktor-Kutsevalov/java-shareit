package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
    }

    public static Item toItem(ItemCreateRequest request, User owner) {
        if (request == null) return null;
        return new Item(null, request.getName(), request.getDescription(), request.getAvailable(), owner, null);
    }
}
