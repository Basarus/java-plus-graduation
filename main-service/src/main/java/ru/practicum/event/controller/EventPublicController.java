package ru.practicum.event.controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.EventsPublicGetRequest;
import ru.practicum.exception.ValidationException;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {
    private final EventService eventService;

    @GetMapping()
    public Collection<EventShortDto> getEventsFiltered(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSortBy sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request) {
        EventsPublicGetRequest getRequest =
                new EventsPublicGetRequest(
                        text,
                        categories,
                        paid,
                        rangeStart,
                        rangeEnd,
                        onlyAvailable,
                        sort,
                        from,
                        size,
                        request);
        if (getRequest.hasRangeStart() && getRequest.hasRangeEnd()) {
            if (getRequest.rangeEnd().isBefore(getRequest.rangeStart())) {
                throw new ValidationException("End date must be before start date");
            }
        }
        log.info("Public get events requested with params= {}", getRequest);
        return eventService.getEvents(getRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("Public get event with eventId={} requested", eventId);
        return eventService.getById(eventId, request);
    }
}
