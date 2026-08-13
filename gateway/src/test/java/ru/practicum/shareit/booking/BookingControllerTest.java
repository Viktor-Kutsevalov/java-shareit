package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void shouldCreateBooking() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );
        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).bookItem(eq(1L), any(BookItemRequestDto.class));
    }

    @Test
    void shouldGetBookingsWithDefaultState() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getBookings(eq(1L), any(), eq(0), eq(10));
    }

    @Test
    void shouldGetBookingsWithCustomParams() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L)
                        .param("state", "WAITING")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getBookings(eq(1L), any(), eq(5), eq(20));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getBooking(1L, 1L);
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).approveBooking(eq(1L), eq(1L), eq(true));
    }
}
