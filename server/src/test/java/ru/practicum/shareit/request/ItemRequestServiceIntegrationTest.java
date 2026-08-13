package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAndGetRequest() {
        User user = new User(null, "Пользователь", "user@mail.com");
        user = userRepository.save(user);

        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна книга");
        ItemRequestDto created = requestService.create(user.getId(), createDto);

        assertNotNull(created.getId());
        assertEquals("Нужна книга", created.getDescription());

        ItemRequestDto found = requestService.getRequestById(user.getId(), created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Нужна книга", found.getDescription());
    }

    @Test
    void shouldGetOtherRequestsWithPagination() throws InterruptedException {
        User user1 = userRepository.save(new User(null, "User1", "user1@mail.com"));
        User user2 = userRepository.save(new User(null, "User2", "user2@mail.com"));

        requestService.create(user2.getId(), new ItemRequestCreateDto("Запрос1"));
        Thread.sleep(10);
        requestService.create(user2.getId(), new ItemRequestCreateDto("Запрос2"));
        Thread.sleep(10);
        requestService.create(user2.getId(), new ItemRequestCreateDto("Запрос3"));

        List<ItemRequestDto> result = requestService.getOtherRequests(user1.getId(), 0, 2);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Запрос3");
    }
}
