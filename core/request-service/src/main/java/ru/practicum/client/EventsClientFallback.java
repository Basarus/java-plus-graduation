package ru.practicum.client;

import ru.practicum.event.dto.EventInfoDto;

import org.springframework.stereotype.Component;

@Component
public class EventsClientFallback implements EventsClient {

    @Override
    public EventInfoDto getEventById(Long id) {
        return null;
    }
}
