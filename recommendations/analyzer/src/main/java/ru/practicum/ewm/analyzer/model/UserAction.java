package ru.practicum.ewm.analyzer.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
@NoArgsConstructor
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_score", nullable = false)
    private Double score;

    @Column(name = "timestamp_action")
    private LocalDateTime timestamp;

    public UserAction(Long userId, Long eventId, Double score, LocalDateTime timestamp) {
        this.userId = userId;
        this.eventId = eventId;
        this.score = score;
        this.timestamp = timestamp;
    }
}
