package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void shouldCreateBooking() throws Exception {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));
        when(bookingService.createBooking(anyLong(), any(BookingCreateDto.class)))
                .thenReturn(new BookingDto(1L, dto.getStart(), dto.getEnd(), "WAITING", null, null));
        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(new BookingDto(1L, LocalDateTime.now(), LocalDateTime.now(), "APPROVED", null, null));
        mockMvc.perform(patch("/bookings/1")
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldGetBooking() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(new BookingDto(1L, LocalDateTime.now(), LocalDateTime.now(), "WAITING", null, null));
        mockMvc.perform(get("/bookings/1")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldGetUserBookings() throws Exception {
        when(bookingService.getUserBookings(anyLong(), anyString()))
                .thenReturn(List.of(new BookingDto()));
        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString()))
                .thenReturn(List.of(new BookingDto()));
        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
