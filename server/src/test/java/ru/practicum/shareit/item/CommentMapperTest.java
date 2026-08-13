package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void shouldMapToCommentDto() {
        User author = new User(1L, "Автор", "author@mail.com");
        Item item = new Item(1L, "Вещь", "Описание", true, new User(), null);
        Comment comment = new Comment(1L, "Хорошая вещь", item, author, LocalDateTime.now());

        CommentDto dto = CommentMapper.toCommentDto(comment);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Хорошая вещь");
        assertThat(dto.getAuthorName()).isEqualTo("Автор");
        assertThat(dto.getCreated()).isNotNull();
    }

    @Test
    void shouldReturnNullWhenCommentNull() {
        assertThat(CommentMapper.toCommentDto(null)).isNull();
    }
}
