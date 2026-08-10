package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingCreateDto dto) {
        User booker;
        try {
            booker = findUser(userId);
        } catch (NotFoundException e) {
            throw new RuntimeException("Пользователь не найден", e);
        }
        Item item = findItem(dto.getItemId());
        validateBooking(item, userId, dto);

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("Создано бронирование {} для вещи {} пользователем {}", saved.getId(), item.getId(), userId);
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = findBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может подтверждать бронирование");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Бронирование {} {} пользователем {}", bookingId, approved ? "подтверждено" : "отклонено", userId);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = findBooking(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является автором или владельцем вещи");
        }
        log.debug("Получено бронирование {} пользователем {}", bookingId, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(Long userId, String state) {
        findUser(userId);
        BookingState stateEnum = BookingState.from(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (stateEnum) {
            case ALL: bookings = bookingRepository.findByBookerId(userId, sort); break;
            case CURRENT: bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, sort); break;
            case PAST: bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, sort); break;
            case FUTURE: bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, sort); break;
            case WAITING: bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort); break;
            case REJECTED: bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort); break;
            default: throw new ValidationException("Неизвестный статус: " + state);
        }
        log.info("Получены бронирования пользователя {} (статус {})", userId, state);
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        findUser(userId);
        BookingState stateEnum = BookingState.from(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (stateEnum) {
            case ALL: bookings = bookingRepository.findAllByOwnerId(userId, sort); break;
            case CURRENT: bookings = bookingRepository.findCurrentByOwnerId(userId, now, sort); break;
            case PAST: bookings = bookingRepository.findPastByOwnerId(userId, now, sort); break;
            case FUTURE: bookings = bookingRepository.findFutureByOwnerId(userId, now, sort); break;
            case WAITING: bookings = bookingRepository.findAllByOwnerIdAndStatus(userId, BookingStatus.WAITING, sort); break;
            case REJECTED: bookings = bookingRepository.findAllByOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort); break;
            default: throw new ValidationException("Неизвестный статус: " + state);
        }
        log.info("Получены бронирования владельца {} (статус {})", userId, state);
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
    }

    private void validateBooking(Item item, Long userId, BookingCreateDto dto) {
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (dto.getStart().isAfter(dto.getEnd()) || dto.getStart().equals(dto.getEnd())) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания");
        }
        if (dto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала должна быть в будущем");
        }
    }
}
