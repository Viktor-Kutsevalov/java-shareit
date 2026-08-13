package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateRequest;
import ru.practicum.shareit.item.dto.ItemUpdateRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void shouldCreateItem() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("Дрель", "Электрическая", true, null);
        when(itemClient.createItem(anyLong(), any(ItemCreateRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).createItem(eq(1L), any(ItemCreateRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenItemNameBlank() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("", "Описание", true, null);
        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionBlank() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("Дрель", "", true, null);
        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenAvailableNull() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest("Дрель", "Описание", null, null);
        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemUpdateRequest request = new ItemUpdateRequest("Новое имя", "Новое описание", false);
        when(itemClient.updateItem(anyLong(), anyLong(), any(ItemUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).updateItem(eq(1L), eq(1L), any(ItemUpdateRequest.class));
    }

    @Test
    void shouldGetItem() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).getItem(1L, 1L);
    }

    @Test
    void shouldGetOwnItems() throws Exception {
        when(itemClient.getItemsByOwner(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).getItemsByOwner(1L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.searchItems(anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).searchItems("дрель");
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto commentDto = new CommentDto(null, "Отличная вещь!", null, null);
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenCommentTextBlank() throws Exception {
        CommentDto commentDto = new CommentDto(null, "", null, null);
        mockMvc.perform(post("/items/1/comment")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}
