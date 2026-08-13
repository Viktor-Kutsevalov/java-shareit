package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient requestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна книга");
        when(requestClient.createRequest(anyLong(), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).createRequest(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionBlank() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("");
        mockMvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).createRequest(anyLong(), any());
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestClient.getOwnRequests(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).getOwnRequests(1L);
    }

    @Test
    void shouldGetOtherRequests() throws Exception {
        when(requestClient.getOtherRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).getOtherRequests(eq(1L), eq(0), eq(10));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestClient.getRequestById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).getRequestById(1L, 1L);
    }
}
