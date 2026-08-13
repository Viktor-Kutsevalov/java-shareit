package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByItemIdOrderByCreatedAsc() {
        User author = userRepository.save(new User(null, "Автор", "author@mail.com"));
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        Item item = itemRepository.save(new Item(null, "Вещь", "Описание", true, owner, null));
        Comment c1 = commentRepository.save(new Comment(null, "Первый", item, author, LocalDateTime.now().minusMinutes(5)));
        Comment c2 = commentRepository.save(new Comment(null, "Второй", item, author, LocalDateTime.now()));

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId());
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getText()).isEqualTo("Первый");
        assertThat(comments.get(1).getText()).isEqualTo("Второй");
    }

    @Test
    void shouldFindByItemIdIn() {
        User author = userRepository.save(new User(null, "Автор", "author@mail.com"));
        User owner = userRepository.save(new User(null, "Владелец", "owner@mail.com"));
        Item item1 = itemRepository.save(new Item(null, "Вещь1", "Описание1", true, owner, null));
        Item item2 = itemRepository.save(new Item(null, "Вещь2", "Описание2", true, owner, null));
        commentRepository.save(new Comment(null, "Коммент1", item1, author, LocalDateTime.now()));
        commentRepository.save(new Comment(null, "Коммент2", item2, author, LocalDateTime.now()));

        List<Comment> comments = commentRepository.findByItemIdIn(List.of(item1.getId(), item2.getId()), Sort.by("created"));
        assertThat(comments).hasSize(2);
    }
}
