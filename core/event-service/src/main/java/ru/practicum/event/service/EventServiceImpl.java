package ru.practicum.event.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.CommentsClient;
import ru.practicum.client.RequestsClient;
import ru.practicum.client.UsersClient;
import ru.practicum.event.controller.EventSortBy;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ForbiddenAccessException;
import ru.practicum.exception.IllegalEventUpdateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.recommendations.AnalyzerGrpcClient;
import ru.practicum.user.dto.UserShortDto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private static final Duration MIN_TIME_BEFORE_EVENT = Duration.ofHours(2);

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final AnalyzerGrpcClient analyzerGrpcClient;
    private final UsersClient usersClient;
    private final RequestsClient requestsClient;
    private final CommentsClient commentsClient;

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        Event event =
                eventRepository
                        .findByIdAndState(eventId, EventState.PUBLISHED)
                        .orElseThrow(
                                NotFoundException.supplier("Event with id=%d not found", eventId));

        Map<Long, Double> ratings = getRatingsOrDefault(List.of(eventId));
        long rating = Math.round(ratings.getOrDefault(eventId, 0.0));
        Map<Long, Long> confirmedRequests = getConfirmedCounts(List.of(event.getId()));
        Map<Long, Long> commentCounts = getCommentCounts(List.of(event.getId()));
        UserShortDto initiator = usersClient.getUserById(event.getInitiatorId());

        return EventMapper.mapToFullDto(
                event,
                confirmedRequests.getOrDefault(event.getId(), 0L),
                rating,
                commentCounts.getOrDefault(event.getId(), 0L),
                initiator);
    }

    @Override
    public Collection<EventShortDto> getEvents(EventsPublicGetRequest getRequest) {
        Page<Event> events =
                eventRepository.findAll(
                        EventRepository.createPredicate(getRequest), getRequest.getPageable());

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Double> ratings = getRatingsOrDefault(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedCounts(eventIds);
        Map<Long, Long> commentCounts = getCommentCounts(eventIds);
        Map<Long, UserShortDto> initiators = getInitiators(events.getContent());

        List<EventShortDto> eventsList =
                events.stream()
                        .map(
                                event ->
                                        EventMapper.mapToShortDto(
                                                event,
                                                confirmedRequests.getOrDefault(event.getId(), 0L),
                                                Math.round(
                                                        ratings.getOrDefault(event.getId(), 0.0)),
                                                commentCounts.getOrDefault(event.getId(), 0L),
                                                initiators.getOrDefault(
                                                        event.getInitiatorId(),
                                                        new UserShortDto(
                                                                event.getInitiatorId(),
                                                                "Unknown"))))
                        .toList();

        if (EventSortBy.VIEWS.equals(getRequest.sort())) {
            return eventsList.stream().sorted(Comparator.comparing(EventShortDto::views)).toList();
        }

        return eventsList;
    }

    @Override
    public Collection<EventFullDto> getEvents(EventsAdminGetRequest getRequest) {
        Page<Event> events =
                eventRepository.findAll(
                        EventRepository.createPredicate(getRequest), getRequest.getPageable());

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Double> ratings = getRatingsOrDefault(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedCounts(eventIds);
        Map<Long, UserShortDto> initiators = getInitiators(events.getContent());

        return events.stream()
                .map(
                        event ->
                                EventMapper.mapToFullDto(
                                        event,
                                        confirmedRequests.getOrDefault(event.getId(), 0L),
                                        Math.round(ratings.getOrDefault(event.getId(), 0.0)),
                                        null,
                                        initiators.getOrDefault(
                                                event.getInitiatorId(),
                                                new UserShortDto(
                                                        event.getInitiatorId(), "Unknown"))))
                .toList();
    }

    @Override
    public Collection<EventShortDto> getEvents(EventsPrivateGetRequest getRequest) {
        UserShortDto initiator = usersClient.getUserById(getRequest.userId());
        Page<Event> events =
                eventRepository.findByInitiatorId(getRequest.userId(), getRequest.getPageable());

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Double> ratings = getRatingsOrDefault(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedCounts(eventIds);

        return events.stream()
                .map(
                        event ->
                                EventMapper.mapToShortDto(
                                        event,
                                        confirmedRequests.getOrDefault(event.getId(), 0L),
                                        Math.round(ratings.getOrDefault(event.getId(), 0.0)),
                                        null,
                                        initiator))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto initiator = usersClient.getUserById(userId);
        Location location = LocationMapper.mapToEntity(newEventDto.location());
        Category category = getCategoryByIdOrThrow(newEventDto.category());
        Event event = EventMapper.mapToEntity(newEventDto, category, userId, location);

        LocalDateTime now = LocalDateTime.now();
        if (event.getEventDate().isBefore(now.plus(MIN_TIME_BEFORE_EVENT))) {
            throw new ValidationException(
                    "The event must be scheduled at least %d hours from now."
                            .formatted(MIN_TIME_BEFORE_EVENT.toHours()));
        }
        Event saved = eventRepository.save(event);

        return EventMapper.mapToFullDto(saved, 0, 0L, 0L, initiator);
    }

    @Override
    public EventFullDto getByUserById(Long userId, Long eventId) {
        UserShortDto initiator = usersClient.getUserById(userId);
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ForbiddenAccessException("You can't view event that's not yours");
        }

        Map<Long, Double> ratings = getRatingsOrDefault(List.of(eventId));
        long rating = Math.round(ratings.getOrDefault(eventId, 0.0));
        Map<Long, Long> confirmedRequests = getConfirmedCounts(List.of(eventId));

        return EventMapper.mapToFullDto(
                event, confirmedRequests.getOrDefault(event.getId(), 0L), rating, null, initiator);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = getEventByIdOrThrow(eventId);

        if ((event.getState().equals(EventState.PUBLISHED)
                        || event.getState().equals(EventState.CANCELED))
                && updateRequest.hasStateAction()) {
            throw new IllegalEventUpdateException(
                    "Forbidden to update event that already %s"
                            .formatted(event.getState().toString()));
        }

        Category newCategory = null;
        if (updateRequest.hasCategory()) {
            newCategory = getCategoryByIdOrThrow(updateRequest.category());
        }
        EventMapper.updateEventFromDto(event, updateRequest, newCategory);
        Event saved = eventRepository.save(event);

        UserShortDto initiator = usersClient.getUserById(saved.getInitiatorId());
        Map<Long, Long> confirmedRequests = getConfirmedCounts(List.of(eventId));

        return EventMapper.mapToFullDto(
                saved, confirmedRequests.getOrDefault(saved.getId(), 0L), null, null, initiator);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(
            Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = getEventByIdOrThrow(eventId);
        UserShortDto initiator = usersClient.getUserById(userId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ForbiddenAccessException("You can't update event that's not yours");
        }

        if ((event.getState().equals(EventState.PUBLISHED)
                        || event.getState().equals(EventState.CANCELED))
                && !updateRequest.hasStateAction()) {
            throw new IllegalEventUpdateException(
                    "Forbidden to update event that already %s"
                            .formatted(event.getState().toString()));
        }

        Category newCategory = null;
        if (updateRequest.hasCategory()) {
            newCategory = getCategoryByIdOrThrow(updateRequest.category());
        }
        EventMapper.updateEventFromDto(event, updateRequest, newCategory);
        Event saved = eventRepository.save(event);

        Map<Long, Long> confirmedRequests = getConfirmedCounts(List.of(eventId));

        return EventMapper.mapToFullDto(
                saved, confirmedRequests.getOrDefault(saved.getId(), 0L), null, null, initiator);
    }

    @Override
    public EventInfoDto getEventInfoById(Long eventId) {
        return EventMapper.mapToInfoDto(getEventByIdOrThrow(eventId));
    }

    @Override
    public Collection<EventInfoDto> getEventInfoByIds(Collection<Long> eventIds) {
        return eventRepository.findAllByIdIn(eventIds).stream()
                .map(EventMapper::mapToInfoDto)
                .toList();
    }

    @Override
    public List<EventCompilationDto> getEventsCompilationData(List<Long> ids) {
        return eventRepository.findAllByIdIn(ids).stream()
                .map(EventMapper::mapToCompilationDto)
                .toList();
    }

    private Map<Long, Double> getRatingsOrDefault(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        try {
            return analyzerGrpcClient.getInteractionsCount(eventIds);
        } catch (Exception e) {
            log.warn("Analyzer unavailable, returning default ratings", e);
            return eventIds.stream().collect(Collectors.toMap(Function.identity(), id -> 0.0));
        }
    }

    private Map<Long, UserShortDto> getInitiators(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        return usersClient.getUsersByIds(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));
    }

    private Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        try {
            return requestsClient.getConfirmedCounts(eventIds);
        } catch (Exception e) {
            log.warn("Could not fetch confirmed counts", e);
            return Map.of();
        }
    }

    private Map<Long, Long> getCommentCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        try {
            return commentsClient.getCommentCounts(eventIds);
        } catch (Exception e) {
            log.warn("Could not fetch comment counts", e);
            return Map.of();
        }
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository
                .findById(eventId)
                .orElseThrow(NotFoundException.supplier("Event with id=%d not found", eventId));
    }

    private Category getCategoryByIdOrThrow(Long categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(
                        NotFoundException.supplier("Category with id=%d not found", categoryId));
    }
}
