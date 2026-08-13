package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;
    private User requestor;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        booker = userRepository.save(new User(null, "Букер", "booker@mail.com"));
        requestor = userRepository.save(new User(null, "Запросчик", "requestor@mail.com"));
    }

    @Test
    void shouldCreateItemWithoutRequest() {
        ItemCreateRequest request = new ItemCreateRequest("Молоток", "Ударный инструмент", true, null);
        ItemDto created = itemService.createItem(owner.getId(), request);
        assertNotNull(created.getId());
        assertEquals("Молоток", created.getName());
        assertTrue(created.getAvailable());
        Item saved = itemRepository.findById(created.getId()).orElseThrow();
        assertEquals(owner.getId(), saved.getOwner().getId());
        assertNull(saved.getRequest());
    }

    @Test
    void shouldCreateItemWithRequest() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Нужна вещь");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = requestRepository.save(itemRequest);
        ItemCreateRequest request = new ItemCreateRequest("Вещь", "Описание", true, itemRequest.getId());
        ItemDto created = itemService.createItem(owner.getId(), request);
        assertNotNull(created.getId());
        Item saved = itemRepository.findById(created.getId()).orElseThrow();
        assertNotNull(saved.getRequest());
        assertEquals(itemRequest.getId(), saved.getRequest().getId());
    }

    @Test
    void shouldUpdateItem() {
        ItemCreateRequest createRequest = new ItemCreateRequest("Дрель", "Старое описание", true, null);
        ItemDto created = itemService.createItem(owner.getId(), createRequest);
        ItemUpdateRequest updateRequest = new ItemUpdateRequest("Новая дрель", "Новое описание", false);
        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(), updateRequest);
        assertEquals("Новая дрель", updated.getName());
        assertEquals("Новое описание", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void shouldThrowWhenUpdateNotOwner() {
        ItemCreateRequest createRequest = new ItemCreateRequest("Дрель", "Описание", true, null);
        ItemDto created = itemService.createItem(owner.getId(), createRequest);
        ItemUpdateRequest updateRequest = new ItemUpdateRequest("Новое имя", null, null);
        assertThatThrownBy(() -> itemService.updateItem(booker.getId(), created.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetItemById() {
        ItemCreateRequest createRequest = new ItemCreateRequest("Дрель", "Электрическая", true, null);
        ItemDto created = itemService.createItem(owner.getId(), createRequest);
        ItemExtendedDto dto = itemService.getItemById(created.getId(), owner.getId());
        assertEquals(created.getId(), dto.getId());
        assertEquals("Дрель", dto.getName());
        assertTrue(dto.getAvailable());
    }

    @Test
    void shouldGetItemsByOwner() {
        ItemCreateRequest request1 = new ItemCreateRequest("Дрель", "Описание1", true, null);
        itemService.createItem(owner.getId(), request1);
        ItemCreateRequest request2 = new ItemCreateRequest("Молоток", "Описание2", true, null);
        itemService.createItem(owner.getId(), request2);
        List<ItemExtendedDto> dtos = itemService.getItemsByOwner(owner.getId());
        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(ItemExtendedDto::getName).containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void shouldSearchItemsByText() {
        ItemCreateRequest request1 = new ItemCreateRequest("Дрель", "Электрическая дрель", true, null);
        itemService.createItem(owner.getId(), request1);
        ItemCreateRequest request2 = new ItemCreateRequest("Перфоратор", "Электрический перфоратор", true, null);
        itemService.createItem(owner.getId(), request2);
        ItemCreateRequest request3 = new ItemCreateRequest("Молоток", "Ручной", false, null);
        itemService.createItem(owner.getId(), request3);

        List<ItemDto> results = itemService.searchItems("электрическ");
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ItemDto::getName).containsExactlyInAnyOrder("Дрель", "Перфоратор");
    }


    @Test
    void shouldReturnEmptyListWhenSearchTextBlank() {
        List<ItemDto> results = itemService.searchItems("");
        assertThat(results).isEmpty();
        results = itemService.searchItems(null);
        assertThat(results).isEmpty();
    }

    @Test
    void shouldAddCommentWithCompletedBooking() {
        ItemCreateRequest createRequest = new ItemCreateRequest("Вещь", "Описание", true, null);
        ItemDto created = itemService.createItem(owner.getId(), createRequest);
        LocalDateTime now = LocalDateTime.now();
        Booking completed = new Booking();
        completed.setStart(now.minusDays(2));
        completed.setEnd(now.minusDays(1));
        completed.setItem(itemRepository.findById(created.getId()).orElseThrow());
        completed.setBooker(booker);
        completed.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(completed);
        CommentDto commentDto = new CommentDto(null, "Отлично", null, null);
        CommentDto saved = itemService.addComment(booker.getId(), created.getId(), commentDto);
        assertNotNull(saved.getId());
        assertEquals("Отлично", saved.getText());
        assertEquals(booker.getName(), saved.getAuthorName());
        assertNotNull(saved.getCreated());
    }

    @Test
    void shouldThrowWhenAddCommentWithoutCompletedBooking() {
        ItemCreateRequest createRequest = new ItemCreateRequest("Вещь", "Описание", true, null);
        ItemDto created = itemService.createItem(owner.getId(), createRequest);
        LocalDateTime now = LocalDateTime.now();
        Booking future = new Booking();
        future.setStart(now.plusDays(1));
        future.setEnd(now.plusDays(2));
        future.setItem(itemRepository.findById(created.getId()).orElseThrow());
        future.setBooker(booker);
        future.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(future);
        CommentDto commentDto = new CommentDto(null, "Отлично", null, null);
        assertThatThrownBy(() -> itemService.addComment(booker.getId(), created.getId(), commentDto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenGetItemNotFound() {
        assertThatThrownBy(() -> itemService.getItemById(999L, owner.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
