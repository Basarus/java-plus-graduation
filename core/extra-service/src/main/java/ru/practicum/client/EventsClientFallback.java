package ru.practicum.client;

import java.util.List;

import ru.practicum.event.dto.EventCompilationDto;
import ru.practicum.event.dto.EventInfoDto;

import org.springframework.stereotype.Component;

@Component
public class EventsClientFallback implements EventsClient {

    @Override
    public EventInfoDto getEventById(Long id) {
        return null;
    }

    @Override
    public List<EventCompilationDto> getEventsCompilationData(List<Long> ids) {
        return List.of();
    }
}
