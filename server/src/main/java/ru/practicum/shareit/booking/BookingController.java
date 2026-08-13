package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody BookingCreateDto dto) {
        log.info("Запрос на создание бронирования от пользователя {}", userId);
        return bookingService.createBooking(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("Запрос на подтверждение бронирования {} от пользователя {}", bookingId, userId);
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long bookingId) {
        log.info("Запрос бронирования {} для пользователя {}", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestParam(defaultValue = "ALL") String state) {
        log.info("Запрос бронирований пользователя {} со статусом {}", userId, state);
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                       @RequestParam(defaultValue = "ALL") String state) {
        log.info("Запрос бронирований владельца {} со статусом {}", userId, state);
        if (userId == null) {
            throw new RuntimeException("Требуется идентификатор пользователя.");
        }
        return bookingService.getOwnerBookings(userId, state);
    }
}
