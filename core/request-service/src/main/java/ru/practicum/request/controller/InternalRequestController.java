package ru.practicum.request.controller;

import java.util.List;
import java.util.Map;

import ru.practicum.request.service.ParticipationRequestService;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class InternalRequestController {

    private final ParticipationRequestService requestService;

    @GetMapping("/confirmed-counts")
    public Map<Long, Long> getConfirmedCounts(@RequestParam List<Long> eventIds) {
        return requestService.getConfirmedCountsByEventIds(eventIds);
    }
}
