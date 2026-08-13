package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void shouldMapToItemDto() {
        Item item = new Item(1L, "Дрель", "Электрическая", true, new User(), null);
        ItemDto dto = ItemMapper.toItemDto(item);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Электрическая");
        assertThat(dto.getAvailable()).isTrue();
    }

    @Test
    void shouldMapToItemFromCreateRequest() {
        User owner = new User(1L, "Владелец", "owner@mail.com");
        ItemCreateRequest request = new ItemCreateRequest("Дрель", "Электрическая", true, null);
        Item item = ItemMapper.toItem(request, owner);
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Электрическая");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isNull();
    }

    @Test
    void shouldReturnNullWhenItemNull() {
        assertThat(ItemMapper.toItemDto(null)).isNull();
        assertThat(ItemMapper.toItem(null, null)).isNull();
    }
}
