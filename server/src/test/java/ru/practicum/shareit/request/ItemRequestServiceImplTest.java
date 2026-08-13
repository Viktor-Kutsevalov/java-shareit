package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<ItemRequest> query;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void shouldCreateRequest() {
        User requestor = new User(1L, "Запросчик", "req@mail.com");
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна книга");
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(new ItemRequest(1L, "Нужна книга", requestor, LocalDateTime.now(), null));

        ItemRequestDto saved = requestService.create(1L, dto);
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getDescription()).isEqualTo("Нужна книга");
    }

    @Test
    void shouldGetOwnRequests() {
        User requestor = new User(1L, "Запросчик", "req@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(
                new ItemRequest(1L, "Запрос1", requestor, LocalDateTime.now(), null)
        ));

        List<ItemRequestDto> dtos = requestService.getOwnRequests(1L);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getDescription()).isEqualTo("Запрос1");
    }

    @Test
    void shouldGetOtherRequestsWithCorrectPagination() {
        User user = new User(1L, "Пользователь", "user@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(entityManager.createQuery(anyString(), eq(ItemRequest.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        List<ItemRequestDto> result = requestService.getOtherRequests(1L, 15, 10);
        assertThat(result).isEmpty();
        verify(query).setFirstResult(15);
        verify(query).setMaxResults(10);
    }

    @Test
    void shouldGetRequestById() {
        User requestor = new User(1L, "Запросчик", "req@mail.com");
        ItemRequest request = new ItemRequest(1L, "Нужна книга", requestor, LocalDateTime.now(), null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        ItemRequestDto dto = requestService.getRequestById(1L, 1L);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна книга");
    }
}
