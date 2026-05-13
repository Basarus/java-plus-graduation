package ru.practicum.event.controller;

import java.util.Collection;
import java.util.List;

import ru.practicum.event.dto.EventCompilationDto;
import ru.practicum.event.dto.EventInfoDto;
import ru.practicum.event.service.EventService;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {
    private final EventService eventService;

    @GetMapping("/{id}")
    public EventInfoDto getEventById(@PathVariable Long id) {
        return eventService.getEventInfoById(id);
    }

    @GetMapping
    public Collection<EventInfoDto> getEventsByIds(@RequestParam List<Long> ids) {
        return eventService.getEventInfoByIds(ids);
    }

    @GetMapping("/compilation")
    public List<EventCompilationDto> getEventsCompilationData(@RequestParam List<Long> ids) {
        return eventService.getEventsCompilationData(ids);
    }
}
