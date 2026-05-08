package ru.practicum.event.dto;

import ru.practicum.event.model.EventState;

public record EventInfoDto(
        Long id,
        EventState state,
        Long initiatorId,
        Integer participantLimit,
        Boolean requestModeration) {}
