package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void shouldCreateBooking() {
        User booker = new User(1L, "Букер", "booker@mail.com");
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, owner, null);
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking(1L, dto.getStart(), dto.getEnd(), item, booker, BookingStatus.WAITING));

        BookingDto saved = bookingService.createBooking(1L, dto);
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo("WAITING");
    }

    @Test
    void shouldThrowWhenItemNotAvailable() {
        User booker = new User(1L, "Букер", "booker@mail.com");
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", false, owner, null);
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(1L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldApproveBooking() {
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, new User(), BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto dto = bookingService.approveBooking(2L, 1L, true);
        assertThat(dto.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void shouldThrowWhenNotOwner() {
        User owner = new User(2L, "Владелец", "owner@mail.com");
        User booker = new User(1L, "Букер", "booker@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Только владелец может подтверждать бронирование");
    }

    @Test
    void shouldGetBookingById() {
        User booker = new User(1L, "Букер", "booker@mail.com");
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto dto = bookingService.getBookingById(1L, 1L);
        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenUserNotBookerOrOwner() {
        User booker = new User(1L, "Букер", "booker@mail.com");
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(3L, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}
