package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void shouldCreateItem() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("Дрель", "Электрическая", true, null);
        when(itemService.createItem(anyLong(), any(ItemCreateRequest.class)))
                .thenReturn(new ItemDto(1L, "Дрель", "Электрическая", true));
        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemUpdateRequest request = new ItemUpdateRequest("Новое имя", "Новое описание", false);
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemUpdateRequest.class)))
                .thenReturn(new ItemDto(1L, "Новое имя", "Новое описание", false));
        mockMvc.perform(patch("/items/1")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void shouldGetItem() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(new ItemExtendedDto(1L, "Дрель", "Описание", true, null, null, null));
        mockMvc.perform(get("/items/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldGetOwnItems() throws Exception {
        when(itemService.getItemsByOwner(anyLong()))
                .thenReturn(List.of(new ItemExtendedDto(1L, "Дрель", "Описание", true, null, null, null)));
        mockMvc.perform(get("/items")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.searchItems("дрель"))
                .thenReturn(List.of(new ItemDto(1L, "Дрель", "Описание", true)));
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto commentDto = new CommentDto(null, "Отлично", null, null);
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(new CommentDto(1L, "Отлично", "Автор", null));
        mockMvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}
