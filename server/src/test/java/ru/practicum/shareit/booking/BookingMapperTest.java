package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void shouldMapToBookingDto() {
        User booker = new User(1L, "Букер", "booker@mail.com");
        User owner = new User(2L, "Владелец", "owner@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, booker, BookingStatus.WAITING);

        BookingDto dto = BookingMapper.toBookingDto(booking);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo("WAITING");
        assertThat(dto.getBooker().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getName()).isEqualTo("Вещь");
    }

    @Test
    void shouldReturnNullWhenBookingNull() {
        assertThat(BookingMapper.toBookingDto(null)).isNull();
    }
}
