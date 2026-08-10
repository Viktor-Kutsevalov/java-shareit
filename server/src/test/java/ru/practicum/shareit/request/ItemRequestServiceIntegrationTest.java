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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceIntegrationTest {

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
}
