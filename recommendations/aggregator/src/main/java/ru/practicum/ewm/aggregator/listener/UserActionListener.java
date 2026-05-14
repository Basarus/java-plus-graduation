package ru.practicum.ewm.aggregator.listener;

import ru.practicum.ewm.aggregator.service.AggregatorService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionListener {

    private final AggregatorService aggregatorService;

    @KafkaListener(
            topics = "${kafka.topics.user-actions}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(UserActionAvro action) {
        log.debug(
                "Received UserActionAvro: userId={}, eventId={}, actionType={}",
                action.getUserId(),
                action.getEventId(),
                action.getActionType());
        aggregatorService.processUserAction(action);
    }
}
