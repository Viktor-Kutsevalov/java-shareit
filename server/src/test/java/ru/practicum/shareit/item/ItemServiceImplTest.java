package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void shouldCreateItem() {
        User owner = new User(1L, "Владелец", "owner@mail.com");
        ItemCreateRequest request = new ItemCreateRequest("Дрель", "Электрическая", true, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(new Item(1L, "Дрель", "Электрическая", true, owner, null));

        ItemDto dto = itemService.createItem(1L, request);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.createItem(1L, new ItemCreateRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateItem() {
        User owner = new User(1L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemUpdateRequest request = new ItemUpdateRequest("Новое имя", "Новое описание", false);
        ItemDto dto = itemService.updateItem(1L, 1L, request);
        assertThat(dto.getName()).isEqualTo("Новое имя");
        assertThat(dto.getDescription()).isEqualTo("Новое описание");
        assertThat(dto.getAvailable()).isFalse();
    }

    @Test
    void shouldThrowWhenUserNotOwner() {
        User owner = new User(1L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> itemService.updateItem(2L, 1L, new ItemUpdateRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetItemById() {
        User owner = new User(1L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Дрель", "Описание", true, owner, null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedAsc(anyLong())).thenReturn(List.of());

        ItemExtendedDto dto = itemService.getItemById(1L, 1L);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
    }

    @Test
    void shouldSearchItems() {
        User owner = new User(1L, "Владелец", "owner@mail.com");
        when(itemRepository.searchByText("дрель")).thenReturn(List.of(
                new Item(1L, "Дрель", "Описание", true, owner, null)
        ));
        List<ItemDto> result = itemService.searchItems("дрель");
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldAddComment() {
        User author = new User(1L, "Автор", "author@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, new User(), null);
        CommentDto commentDto = new CommentDto(null, "Отлично", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findCompletedBookingsForComment(anyLong(), anyLong(), any()))
                .thenReturn(List.of(new Booking()));
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment(1L, "Отлично", item, author, LocalDateTime.now()));

        CommentDto saved = itemService.addComment(1L, 1L, commentDto);
        assertThat(saved.getText()).isEqualTo("Отлично");
        assertThat(saved.getAuthorName()).isEqualTo("Автор");
    }

    @Test
    void shouldThrowWhenNoCompletedBooking() {
        User author = new User(1L, "Автор", "author@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, new User(), null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findCompletedBookingsForComment(anyLong(), anyLong(), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> itemService.addComment(1L, 1L, new CommentDto()))
                .isInstanceOf(ValidationException.class);
    }
}
