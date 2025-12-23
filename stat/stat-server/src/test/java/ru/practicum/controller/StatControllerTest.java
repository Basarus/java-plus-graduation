package ru.practicum.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(StatController.class)
class StatControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private StatService statService;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Test POST /hit")
    void createEndpointHit_WithValidDto_ShouldReturn201() throws Exception {
        EndpointHitDto dto =
                EndpointHitDto.builder()
                        .app("app")
                        .uri("/uri")
                        .ip("1.2.3.4")
                        .timestamp(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
                        .build();

        mockMvc.perform(
                        post("/hit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(statService, times(1)).createEndpointHit(any(EndpointHitDto.class));
    }

    @Test
    @DisplayName("Test POST /hit, missing required field app")
    void createEndpointHit_WithInvalidDto_ShouldReturn400() throws Exception {
        String invalidJson =
                "{\"uri\": \"/test\", \"ip\": \"127.0.0.1\", \"created\": \"2025-01-01 12:00:00\"}";

        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(statService, never()).createEndpointHit(any(EndpointHitDto.class));
    }

    @Test
    @DisplayName("Test GET /stats")
    void getStats_WithValidParams_ShouldReturnStats() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        List<String> uris = List.of("/uri1", "/uri2");
        Boolean unique = true;

        ViewStatsDto viewStatsDto = new ViewStatsDto("app", "/uri1", 5L);
        when(statService.getStat(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(viewStatsDto));

        mockMvc.perform(
                        get("/stats")
                                .param("start", "2025-01-01 10:00:00")
                                .param("end", "2025-01-01 12:00:00")
                                .param("uris", "/uri1", "/uri2")
                                .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("app"))
                .andExpect(jsonPath("$[0].uri").value("/uri1"))
                .andExpect(jsonPath("$[0].hits").value(5));

        verify(statService, times(1)).getStat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test GET /stats with default unique value")
    void getStats_WithMissingUrisAndUnique_ShouldUseDefaults() throws Exception {
        when(statService.getStat(any(), any(), isNull(), eq(false)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                        get("/stats")
                                .param("start", "2025-01-01 10:00:00")
                                .param("end", "2025-01-01 12:00:00"))
                .andExpect(status().isOk());

        verify(statService, times(1)).getStat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test GET /stats with invalid start date format")
    void getStats_WithInvalidDateTimeFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(
                        get("/stats")
                                .param("start", "01-01-2025 10:00")
                                .param("end", "2025-01-01 12:00:00"))
                .andExpect(status().isBadRequest());

        verify(statService, never()).getStat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test GET /stats with absent start value")
    void getStats_WithMissingStartParam_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/stats").param("end", "2025-01-01 12:00:00"))
                .andExpect(status().isBadRequest());

        verify(statService, never()).getStat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test GET /stats with absent end value")
    void getStats_WithMissingEndParam_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/stats").param("start", "2025-01-01 12:00:00"))
                .andExpect(status().isBadRequest());

        verify(statService, never()).getStat(any(), any(), any(), any());
    }
}
