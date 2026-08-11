package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна книга");
        when(requestService.create(anyLong(), any(ItemRequestCreateDto.class)))
                .thenReturn(new ItemRequestDto(1L, "Нужна книга", LocalDateTime.now(), null));
        mockMvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestService.getOwnRequests(anyLong()))
                .thenReturn(List.of(new ItemRequestDto(1L, "Нужна книга", LocalDateTime.now(), null)));
        mockMvc.perform(get("/requests")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldGetOtherRequests() throws Exception {
        when(requestService.getOtherRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(new ItemRequestDto(1L, "Нужна книга", LocalDateTime.now(), null)));
        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(new ItemRequestDto(1L, "Нужна книга", LocalDateTime.now(), null));
        mockMvc.perform(get("/requests/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}
