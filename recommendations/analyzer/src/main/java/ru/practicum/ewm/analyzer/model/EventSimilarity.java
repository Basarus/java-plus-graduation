package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_similarities")
@IdClass(EventSimilarityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventSimilarity {

    @Id
    @Column(name = "first_event", nullable = false)
    private Long first;

    @Id
    @Column(name = "second_event", nullable = false)
    private Long second;

    @Column(name = "score", nullable = false)
    private Double score;
}
