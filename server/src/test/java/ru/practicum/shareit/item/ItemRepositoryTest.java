package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByOwnerId() {
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        Item item1 = itemRepository.save(new Item(null, "Дрель", "Описание1", true, owner, null));
        Item item2 = itemRepository.save(new Item(null, "Молоток", "Описание2", true, owner, null));

        List<Item> items = itemRepository.findByOwnerId(owner.getId());
        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName).containsExactlyInAnyOrder("Дрель", "Молоток");
    }

    @Test
    void shouldSearchByText() {
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        itemRepository.save(new Item(null, "Дрель", "Электрический инструмент", true, owner, null));
        itemRepository.save(new Item(null, "Молоток", "Ударный инструмент", false, owner, null));

        List<Item> result = itemRepository.searchByText("электрический");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель");
    }
}
