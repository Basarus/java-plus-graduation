package ru.practicum.ewm.analyzer.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.practicum.ewm.analyzer.model.EventSimilarity;
import ru.practicum.ewm.analyzer.model.EventSimilarityId;
import ru.practicum.ewm.analyzer.model.UserAction;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void saveUserAction(UserActionAvro action) {
        double weight = toWeight(action.getActionType());
        LocalDateTime timestamp = LocalDateTime.ofInstant(action.getTimestamp(), ZoneOffset.UTC);

        userActionRepository
                .findByUserIdAndEventId(action.getUserId(), action.getEventId())
                .ifPresentOrElse(
                        existing -> {
                            if (weight > existing.getScore()) {
                                existing.setScore(weight);
                                existing.setTimestamp(timestamp);
                            }
                        },
                        () ->
                                userActionRepository.save(
                                        new UserAction(
                                                action.getUserId(),
                                                action.getEventId(),
                                                weight,
                                                timestamp)));

        log.debug(
                "Saved UserAction: userId={}, eventId={}, weight={}",
                action.getUserId(),
                action.getEventId(),
                weight);
    }

    @Transactional
    public void saveEventSimilarity(EventSimilarityAvro similarity) {
        EventSimilarityId id =
                new EventSimilarityId(similarity.getEventA(), similarity.getEventB());
        eventSimilarityRepository.deleteById(id);
        eventSimilarityRepository.save(
                new EventSimilarity(
                        similarity.getEventA(), similarity.getEventB(), similarity.getScore()));
        log.debug(
                "Saved EventSimilarity: eventA={}, eventB={}, score={}",
                similarity.getEventA(),
                similarity.getEventB(),
                similarity.getScore());
    }

    @Transactional(readOnly = true)
    public List<ScoredEvent> getInteractionsCount(List<Long> eventIds) {
        Map<Long, Double> sums =
                userActionRepository.findAllByEventIdIn(eventIds).stream()
                        .collect(
                                Collectors.groupingBy(
                                        UserAction::getEventId,
                                        Collectors.summingDouble(UserAction::getScore)));

        return eventIds.stream()
                .map(eventId -> new ScoredEvent(eventId, sums.getOrDefault(eventId, 0.0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScoredEvent> getRecommendationsForUser(long userId, int maxResults) {
        List<Long> recentEventIds = userActionRepository.findRecentEventIdsByUserId(userId, 10);
        if (recentEventIds.isEmpty()) return List.of();

        List<Long> interactedEventIds =
                userActionRepository.findAllInteractionsByUser(userId).stream()
                        .map(UserAction::getEventId)
                        .toList();

        return eventSimilarityRepository
                .findSimilarUnseenEvents(recentEventIds, interactedEventIds, maxResults)
                .stream()
                .map(
                        es -> {
                            long candidateId =
                                    recentEventIds.contains(es.getFirst())
                                            ? es.getSecond()
                                            : es.getFirst();
                            return new ScoredEvent(candidateId, es.getScore());
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScoredEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        return eventSimilarityRepository.findByEventIdForUser(eventId, userId, maxResults).stream()
                .map(
                        es -> {
                            long similarId =
                                    es.getFirst().equals(eventId) ? es.getSecond() : es.getFirst();
                            return new ScoredEvent(similarId, es.getScore());
                        })
                .toList();
    }

    private double toWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
