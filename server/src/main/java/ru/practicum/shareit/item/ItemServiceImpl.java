package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto createItem(Long userId, ItemCreateRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = ItemMapper.toItem(request, owner);
        if (request.getRequestId() != null) {
            ItemRequest itemRequest = requestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(itemRequest);
            log.info("Вещь создана в ответ на запрос {}", request.getRequestId());
        }
        log.info("Создана вещь {} для пользователя {}", item.getName(), userId);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }
        updateItemFields(item, request);
        log.info("Обновлена вещь {} пользователем {}", itemId, userId);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    private void updateItemFields(Item item, ItemUpdateRequest request) {
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ItemExtendedDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId);
            nextBooking = getNextBooking(itemId);
        }

        log.debug("Получена информация о вещи {} для пользователя {}", itemId, userId);
        return new ItemExtendedDto(
                item.getId(), item.getName(), item.getDescription(),
                item.getAvailable(), lastBooking, nextBooking,
                comments != null ? comments : Collections.emptyList()  // гарантируем не-null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemExtendedDto> getItemsByOwner(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) return Collections.emptyList();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        List<Comment> allComments = commentRepository.findByItemIdIn(itemIds, Sort.by("created").ascending());
        Map<Long, List<CommentDto>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingRepository.findLastApprovedByItemIdIn(itemIds, now, Sort.by("start").descending());
        Map<Long, BookingShortDto> lastByItemId = lastBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> new BookingShortDto(b.getId(), b.getStart(), b.getEnd(), b.getBooker().getId()),
                        (e, r) -> e
                ));

        List<Booking> nextBookings = bookingRepository.findNextApprovedByItemIdIn(itemIds, now, Sort.by("start").ascending());
        Map<Long, BookingShortDto> nextByItemId = nextBookings.stream()
                .collect(Collectors.toMap(
                        b -> b.getItem().getId(),
                        b -> new BookingShortDto(b.getId(), b.getStart(), b.getEnd(), b.getBooker().getId()),
                        (e, r) -> e
                ));

        log.info("Получены все вещи пользователя {} (всего {})", userId, items.size());
        return items.stream()
                .map(item -> new ItemExtendedDto(
                        item.getId(), item.getName(), item.getDescription(),
                        item.getAvailable(),
                        lastByItemId.get(item.getId()),
                        nextByItemId.get(item.getId()),
                        commentsByItemId.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        log.debug("Поиск вещей по тексту: {}", text);
        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> completedBookings = bookingRepository
                .findCompletedBookingsForComment(userId, itemId, now);

        if (completedBookings.isEmpty()) {
            throw new ValidationException("Нет завершённых бронирований для этой вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private BookingShortDto getLastBooking(Long itemId) {
        List<Booking> bookings = bookingRepository.findLastApprovedByItemId(itemId, LocalDateTime.now());
        if (bookings.isEmpty()) return null;
        Booking b = bookings.get(0);
        return new BookingShortDto(b.getId(), b.getStart(), b.getEnd(), b.getBooker().getId());
    }

    private BookingShortDto getNextBooking(Long itemId) {
        List<Booking> bookings = bookingRepository.findNextApprovedByItemId(itemId, LocalDateTime.now());
        if (bookings.isEmpty()) return null;
        Booking b = bookings.get(0);
        return new BookingShortDto(b.getId(), b.getStart(), b.getEnd(), b.getBooker().getId());
    }
}
