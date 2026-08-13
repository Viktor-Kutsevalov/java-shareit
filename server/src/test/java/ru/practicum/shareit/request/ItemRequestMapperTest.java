package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void shouldMapToItemRequest() {
        User requestor = new User(1L, "Запросчик", "req@mail.com");
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна книга");
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);
        assertThat(request.getDescription()).isEqualTo("Нужна книга");
        assertThat(request.getRequestor()).isEqualTo(requestor);
        assertThat(request.getCreated()).isNotNull();
    }

    @Test
    void shouldMapToItemRequestDto() {
        User requestor = new User(1L, "Запросчик", "req@mail.com");
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Книга", "Описание", true, owner, null);
        ItemRequest request = new ItemRequest(1L, "Нужна книга", requestor, LocalDateTime.now(), List.of(item));

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна книга");
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Книга");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void shouldHandleNullItems() {
        ItemRequest request = new ItemRequest(1L, "Нужна книга", new User(), LocalDateTime.now(), null);
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        assertThat(dto.getItems()).isNull();
    }
}
