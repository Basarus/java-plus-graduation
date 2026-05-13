package ru.practicum.ewm.analyzer.repository;

import java.util.List;
import java.util.Optional;

import ru.practicum.ewm.analyzer.model.UserAction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);

    List<UserAction> findAllByEventIdIn(List<Long> eventIds);

    @Query(
            "SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId ORDER BY ua.timestamp"
                    + " DESC LIMIT :limit")
    List<Long> findRecentEventIdsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Query("SELECT ua FROM UserAction ua WHERE ua.userId = :userId")
    List<UserAction> findAllInteractionsByUser(@Param("userId") Long userId);

    @Query(
            """
            select coalesce(sum(a.score), 0)
            from UserAction a
            where a.eventId = :eventId
            """)
    Double sumScoreByEventId(@Param("eventId") Long eventId);
}
