package ru.practicum.client;

import java.util.List;

import ru.practicum.event.dto.EventCompilationDto;
import ru.practicum.event.dto.EventInfoDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service", fallback = EventsClientFallback.class)
public interface EventsClient {

    @GetMapping("/internal/events/{id}")
    EventInfoDto getEventById(@PathVariable Long id);

    @GetMapping("/internal/events/compilation")
    List<EventCompilationDto> getEventsCompilationData(@RequestParam("ids") List<Long> ids);
}
