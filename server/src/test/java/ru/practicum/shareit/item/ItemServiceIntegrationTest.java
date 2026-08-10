package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository; // добавлено

    @Test
    void shouldCreateItemWithRequest() {
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        User requestor = userRepository.save(new User(null, "Запросчик", "req@mail.com"));
        ItemRequest request = new ItemRequest();
        request.setDescription("Нужна вещь");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        request = requestRepository.save(request);

        ItemCreateRequest createDto = new ItemCreateRequest("Вещь", "Описание", true, request.getId());
        ItemDto itemDto = itemService.createItem(owner.getId(), createDto);

        assertNotNull(itemDto.getId());
        assertEquals("Вещь", itemDto.getName());

        Item savedItem = itemRepository.findById(itemDto.getId()).orElseThrow();
        assertNotNull(savedItem.getRequest());
        assertEquals(request.getId(), savedItem.getRequest().getId());
    }
}
