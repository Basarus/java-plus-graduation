package ru.practicum.ewm.aggregator.service;

import java.time.Instant;
import java.util.Set;

import ru.practicum.ewm.aggregator.model.SimilarityMatrix;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorService {

    @Value("${kafka.topics.event-similarity}")
    private String eventSimilarityTopic;

    private final SimilarityMatrix matrix;
    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;

    public void processUserAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = toWeight(action.getActionType());
        double oldWeight = matrix.getWeight(userId, eventId);

        boolean updated = matrix.updateWeight(userId, eventId, newWeight);
        if (!updated) return;

        Set<Long> allEvents = matrix.getAllEvents();
        Instant timestamp = action.getTimestamp();

        for (long otherEventId : allEvents) {
            if (otherEventId == eventId) continue;
            double otherWeight = matrix.getWeight(userId, otherEventId);
            if (otherWeight == 0.0) continue;

            matrix.updateMinWeightSum(eventId, otherEventId, oldWeight, newWeight, otherWeight);
            double score = matrix.computeSimilarity(eventId, otherEventId);
            if (score > 0.0) {
                sendSimilarity(eventId, otherEventId, score, timestamp);
            }
        }
    }

    private void sendSimilarity(long eventA, long eventB, double score, Instant timestamp) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        EventSimilarityAvro avro =
                EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(score)
                        .setTimestamp(timestamp)
                        .build();

        kafkaTemplate.send(eventSimilarityTopic, first, avro);
        log.debug("Sent EventSimilarityAvro: eventA={}, eventB={}, score={}", first, second, score);
    }

    private double toWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
