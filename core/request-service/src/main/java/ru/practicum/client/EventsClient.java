package ru.practicum.client;

import ru.practicum.event.dto.EventInfoDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", fallback = EventsClientFallback.class)
public interface EventsClient {

    @GetMapping("/internal/events/{id}")
    EventInfoDto getEventById(@PathVariable Long id);
}
