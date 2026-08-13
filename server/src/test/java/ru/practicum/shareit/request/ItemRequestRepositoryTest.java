package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByRequestorIdOrderByCreatedDesc() {
        User requestor = userRepository.save(new User(null, "Запросчик", "req@mail.com"));
        requestRepository.save(new ItemRequest(null, "Первый", requestor, LocalDateTime.now().minusDays(1), null));
        requestRepository.save(new ItemRequest(null, "Второй", requestor, LocalDateTime.now(), null));

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(requestor.getId());
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Второй");
    }

    @Test
    void shouldFindAllByRequestorIdNotWithPagination() {
        User user1 = userRepository.save(new User(null, "User1", "user1@mail.com"));
        User user2 = userRepository.save(new User(null, "User2", "user2@mail.com"));
        requestRepository.save(new ItemRequest(null, "Запрос1", user1, LocalDateTime.now(), null));
        requestRepository.save(new ItemRequest(null, "Запрос2", user2, LocalDateTime.now(), null));

        Pageable pageable = PageRequest.of(0, 1);
        List<ItemRequest> result = requestRepository.findAllByRequestorIdNot(user1.getId(), pageable);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequestor().getId()).isEqualTo(user2.getId());
    }
}
