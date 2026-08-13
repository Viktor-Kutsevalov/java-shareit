package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User(null, "Иван", "ivan@mail.com");
        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Иван");
        assertThat(found.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void shouldCheckEmailExists() {
        userRepository.save(new User(null, "Иван", "ivan@mail.com"));
        assertThat(userRepository.existsByEmail("ivan@mail.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@mail.com")).isFalse();
    }

    @Test
    void shouldCheckEmailExistsAndIdNot() {
        User user = userRepository.save(new User(null, "Иван", "ivan@mail.com"));
        assertThat(userRepository.existsByEmailAndIdNot("ivan@mail.com", user.getId())).isFalse();
        assertThat(userRepository.existsByEmailAndIdNot("ivan@mail.com", 999L)).isTrue();
    }
}
