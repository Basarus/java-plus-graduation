package ru.practicum.recommendations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ru.practicum.client.UsersClient;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.dto.UserShortDto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final AnalyzerGrpcClient analyzerGrpcClient;
    private final EventRepository eventRepository;
    private final UsersClient usersClient;

    public List<EventShortDto> getRecommendationsForUser(long userId, int maxResults) {
        List<Long> eventIds = analyzerGrpcClient.getRecommendationsForUser(userId, maxResults);
        if (eventIds.isEmpty()) return List.of();
        return fetchAndMapEvents(eventIds);
    }

    public List<EventShortDto> getSimilarEvents(long eventId, long userId, int maxResults) {
        List<Long> eventIds = analyzerGrpcClient.getSimilarEvents(eventId, userId, maxResults);
        if (eventIds.isEmpty()) return List.of();
        return fetchAndMapEvents(eventIds);
    }

    private List<EventShortDto> fetchAndMapEvents(List<Long> eventIds) {
        Set<Event> events = eventRepository.findAllByIdIn(eventIds);
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        Map<Long, UserShortDto> initiators;
        try {
            initiators =
                    usersClient.getUsersByIds(initiatorIds).stream()
                            .collect(Collectors.toMap(UserShortDto::id, u -> u));
        } catch (Exception e) {
            log.warn("Failed to fetch initiators: {}", e.getMessage());
            initiators = Map.of();
        }
        Map<Long, UserShortDto> finalInitiators = initiators;
        return events.stream()
                .map(
                        e ->
                                EventMapper.mapToShortDto(
                                        e,
                                        0L,
                                        null,
                                        0L,
                                        finalInitiators.getOrDefault(e.getInitiatorId(), null)))
                .toList();
    }
}
