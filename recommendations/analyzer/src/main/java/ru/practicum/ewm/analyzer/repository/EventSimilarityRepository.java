package ru.practicum.ewm.analyzer.repository;

import java.util.List;

import ru.practicum.ewm.analyzer.model.EventSimilarity;
import ru.practicum.ewm.analyzer.model.EventSimilarityId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventSimilarityRepository
        extends JpaRepository<EventSimilarity, EventSimilarityId> {

    @Query(
            """
            SELECT es FROM EventSimilarity es
            WHERE (es.first = :eventId OR es.second = :eventId)
            AND NOT EXISTS (
                SELECT 1 FROM UserAction ua WHERE ua.userId = :userId
                AND ua.eventId = CASE WHEN es.first = :eventId THEN es.second ELSE es.first END
            )
            ORDER BY es.score DESC LIMIT :limit
            """)
    List<EventSimilarity> findByEventIdForUser(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId,
            @Param("limit") int limit);

    @Query(
            """
            SELECT es FROM EventSimilarity es
            WHERE (es.first IN :recentEventIds AND es.second NOT IN :interactedEventIds)
            OR (es.second IN :recentEventIds AND es.first NOT IN :interactedEventIds)
            ORDER BY es.score DESC LIMIT :limit
            """)
    List<EventSimilarity> findSimilarUnseenEvents(
            @Param("recentEventIds") List<Long> recentEventIds,
            @Param("interactedEventIds") List<Long> interactedEventIds,
            @Param("limit") int limit);

    @Query(
            """
            SELECT es FROM EventSimilarity es
            WHERE (es.first = :targetEventId AND es.second IN :userEventIds)
            OR (es.second = :targetEventId AND es.first IN :userEventIds)
            ORDER BY es.score DESC LIMIT :limit
            """)
    List<EventSimilarity> findTopKSimilarUserEvents(
            @Param("targetEventId") Long targetEventId,
            @Param("userEventIds") List<Long> userEventIds,
            @Param("limit") int limit);
}
