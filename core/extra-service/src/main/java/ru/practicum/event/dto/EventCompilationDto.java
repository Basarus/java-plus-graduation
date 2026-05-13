package ru.practicum.event.dto;

import java.time.LocalDateTime;

import ru.practicum.category.dto.CategoryDto;

public record EventCompilationDto(
        Long id,
        String annotation,
        CategoryDto category,
        LocalDateTime eventDate,
        Long initiatorId,
        boolean paid,
        String title) {}
