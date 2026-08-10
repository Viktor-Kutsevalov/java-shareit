package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long userId, ItemCreateRequest request);

    ItemDto updateItem(Long userId, Long itemId, ItemUpdateRequest request);

    ItemExtendedDto getItemById(Long itemId, Long userId);

    List<ItemExtendedDto> getItemsByOwner(Long userId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
