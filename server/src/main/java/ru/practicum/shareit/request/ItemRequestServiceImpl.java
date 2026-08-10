package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);
        ItemRequest saved = requestRepository.save(request);
        log.info("Создан запрос {} от пользователя {}", saved.getId(), userId);
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        log.info("Получены запросы пользователя {} (всего {})", userId, requests.size());
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOtherRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNot(userId, pageable);
        log.info("Получены чужие запросы для пользователя {} (from={}, size={})", userId, from, size);
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        log.debug("Получен запрос {} для пользователя {}", requestId, userId);
        return ItemRequestMapper.toItemRequestDto(request);
    }
}
