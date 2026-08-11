package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldFindByBookerId() {
        User booker = userRepository.save(new User(null, "Букер", "booker@mail.com"));
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        Item item = itemRepository.save(new Item(null, "Вещь", "Описание", true, owner, null));
        Booking booking = bookingRepository.save(new Booking(null, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, booker, BookingStatus.WAITING));

        List<Booking> bookings = bookingRepository.findByBookerId(booker.getId(), Sort.by(Sort.Direction.DESC, "start"));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldFindCompletedBookingsForComment() {
        User booker = userRepository.save(new User(null, "Букер", "booker@mail.com"));
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        Item item = itemRepository.save(new Item(null, "Вещь", "Описание", true, owner, null));
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED));
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item, booker, BookingStatus.APPROVED));

        List<Booking> completed = bookingRepository.findCompletedBookingsForComment(booker.getId(), item.getId(), LocalDateTime.now());
        assertThat(completed).hasSize(1);
        assertThat(completed.get(0).getEnd()).isBefore(LocalDateTime.now());
    }
}
