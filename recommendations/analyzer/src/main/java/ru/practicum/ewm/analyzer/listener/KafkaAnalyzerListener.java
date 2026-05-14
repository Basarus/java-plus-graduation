package ru.practicum.ewm.analyzer.listener;

import ru.practicum.ewm.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAnalyzerListener {

    private final RecommendationService recommendationService;

    @KafkaListener(
            topics = "${kafka.topics.user-actions}",
            containerFactory = "userActionListenerContainerFactory")
    public void listenUserActions(UserActionAvro action) {
        log.debug(
                "Analyzer received UserActionAvro: userId={}, eventId={}",
                action.getUserId(),
                action.getEventId());
        recommendationService.saveUserAction(action);
    }

    @KafkaListener(
            topics = "${kafka.topics.event-similarity}",
            containerFactory = "eventSimilarityListenerContainerFactory")
    public void listenEventSimilarity(EventSimilarityAvro similarity) {
        if (similarity == null) return;
        log.debug(
                "Analyzer received EventSimilarityAvro: eventA={}, eventB={}, score={}",
                similarity.getEventA(),
                similarity.getEventB(),
                similarity.getScore());
        recommendationService.saveEventSimilarity(similarity);
    }
}
