package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeDeserializeBookingDto() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.parse("2025-01-01T10:00:00"));
        dto.setEnd(LocalDateTime.parse("2025-01-02T10:00:00"));
        dto.setStatus("WAITING");

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).contains("2025-01-01T10:00:00");

        BookingDto deserialized = objectMapper.readValue(json, BookingDto.class);
        assertThat(deserialized.getStart()).isEqualTo("2025-01-01T10:00:00");
    }
}
