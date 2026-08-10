package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) return null;
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus().name());

        User booker = booking.getBooker();
        if (booker != null) {
            dto.setBooker(new BookingDto.Booker(booker.getId()));
        }

        Item item = booking.getItem();
        if (item != null) {
            dto.setItem(new BookingDto.Item(item.getId(), item.getName()));
        }

        return dto;
    }
}
