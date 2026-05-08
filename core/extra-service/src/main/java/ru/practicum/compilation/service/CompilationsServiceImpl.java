package ru.practicum.compilation.service;

import java.util.stream.Collectors;
import java.util.*;

import ru.practicum.client.EventsClient;
import ru.practicum.client.RequestsClient;
import ru.practicum.client.UsersClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationsMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationsRepository;
import ru.practicum.event.dto.EventCompilationDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserShortDto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationsServiceImpl implements CompilationsService {

    private final CompilationsRepository compRepository;
    private final EventsClient eventsClient;
    private final UsersClient usersClient;
    private final RequestsClient requestsClient;

    @Override
    public Collection<CompilationDto> findAll(CompilationsPublicGetRequest getRequest) {
        Page<Compilation> page =
                getRequest.pinned() != null
                        ? compRepository.findAllByPinned(
                                getRequest.pinned(), getRequest.getPageable())
                        : compRepository.findAll(getRequest.getPageable());

        Set<Long> allEventIds =
                page.stream().flatMap(c -> c.getEventIds().stream()).collect(Collectors.toSet());

        Map<Long, EventCompilationDto> eventsMap = fetchEventsMap(allEventIds);
        Map<Long, UserShortDto> usersMap = fetchUsersMap(eventsMap.values());
        Map<Long, Long> confirmedCounts = fetchConfirmedCounts(allEventIds);

        return page.stream().map(c -> toDto(c, eventsMap, usersMap, confirmedCounts)).toList();
    }

    @Override
    public CompilationDto findById(long compId) {
        Compilation compilation =
                compRepository
                        .findById(compId)
                        .orElseThrow(
                                NotFoundException.supplier(
                                        "Compilation with id=%d was not found", compId));

        return buildDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto save(NewCompilationDto dto) {
        if (compRepository.existsByTitle(dto.title())) {
            throw new ConflictException(
                    "Compilation with title=" + dto.title() + " already exists");
        }

        Set<Long> eventIds = validateAndExtractIds(dto.events());
        Compilation saved = compRepository.save(CompilationsMapper.mapToEntity(dto, eventIds));
        return buildDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(long compId) {
        if (!compRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto update(long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation =
                compRepository
                        .findById(compId)
                        .orElseThrow(
                                NotFoundException.supplier(
                                        "Compilation with id=%d was not found", compId));

        Set<Long> eventIds = null;
        if (updateRequest.hasEvents()) {
            eventIds = validateAndExtractIds(updateRequest.events());
        }

        CompilationsMapper.updateEntity(compilation, updateRequest, eventIds);
        Compilation updated = compRepository.save(compilation);
        return buildDto(updated);
    }

    private CompilationDto buildDto(Compilation compilation) {
        Set<Long> eventIds = compilation.getEventIds();
        if (eventIds.isEmpty()) {
            return CompilationsMapper.mapToDto(compilation, List.of());
        }

        Map<Long, EventCompilationDto> eventsMap = fetchEventsMap(eventIds);
        Map<Long, UserShortDto> usersMap = fetchUsersMap(eventsMap.values());
        Map<Long, Long> confirmedCounts = fetchConfirmedCounts(eventIds);

        List<EventShortDto> shortDtos = toShortDtos(eventIds, eventsMap, usersMap, confirmedCounts);
        return CompilationsMapper.mapToDto(compilation, shortDtos);
    }

    private CompilationDto toDto(
            Compilation compilation,
            Map<Long, EventCompilationDto> eventsMap,
            Map<Long, UserShortDto> usersMap,
            Map<Long, Long> confirmedCounts) {
        List<EventShortDto> shortDtos =
                toShortDtos(compilation.getEventIds(), eventsMap, usersMap, confirmedCounts);
        return CompilationsMapper.mapToDto(compilation, shortDtos);
    }

    private List<EventShortDto> toShortDtos(
            Set<Long> eventIds,
            Map<Long, EventCompilationDto> eventsMap,
            Map<Long, UserShortDto> usersMap,
            Map<Long, Long> confirmedCounts) {
        return eventIds.stream()
                .map(eventsMap::get)
                .filter(Objects::nonNull)
                .map(
                        e ->
                                new EventShortDto(
                                        e.annotation(),
                                        e.category(),
                                        confirmedCounts.getOrDefault(e.id(), 0L),
                                        e.eventDate(),
                                        e.id(),
                                        usersMap.getOrDefault(
                                                e.initiatorId(),
                                                new UserShortDto(e.initiatorId(), "Unknown")),
                                        e.paid(),
                                        e.title(),
                                        null,
                                        null))
                .toList();
    }

    private Set<Long> validateAndExtractIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        List<EventCompilationDto> events = eventsClient.getEventsCompilationData(ids);
        if (events.size() != ids.size()) {
            throw new NotFoundException("One or more events were not found");
        }
        return events.stream().map(EventCompilationDto::id).collect(Collectors.toSet());
    }

    private Map<Long, EventCompilationDto> fetchEventsMap(Set<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return eventsClient.getEventsCompilationData(new ArrayList<>(eventIds)).stream()
                .collect(Collectors.toMap(EventCompilationDto::id, e -> e));
    }

    private Map<Long, UserShortDto> fetchUsersMap(Collection<EventCompilationDto> events) {
        List<Long> initiatorIds =
                events.stream().map(EventCompilationDto::initiatorId).distinct().toList();
        if (initiatorIds.isEmpty()) {
            return Map.of();
        }
        return usersClient.getUsersByIds(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::id, u -> u));
    }

    private Map<Long, Long> fetchConfirmedCounts(Set<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return requestsClient.getConfirmedCounts(new ArrayList<>(eventIds));
    }
}
