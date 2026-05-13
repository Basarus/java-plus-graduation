package ru.practicum.ewm.collector.service;

import java.time.Instant;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.proto.ActionTypeProto;
import ru.practicum.ewm.stats.collector.proto.UserActionProto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    @Value("${kafka.topics.user-actions}")
    private String userActionsTopic;

    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;

    public void collectUserAction(UserActionProto proto) {
        UserActionAvro avro =
                UserActionAvro.newBuilder()
                        .setUserId(proto.getUserId())
                        .setEventId(proto.getEventId())
                        .setActionType(toAvroActionType(proto.getActionType()))
                        .setTimestamp(toInstant(proto))
                        .build();

        try {
            kafkaTemplate.send(userActionsTopic, avro.getUserId(), avro).get();
            log.debug(
                    "Sent UserAction to Kafka: userId={}, eventId={}, actionType={}",
                    avro.getUserId(),
                    avro.getEventId(),
                    avro.getActionType());
        } catch (Exception e) {
            log.error(
                    "Failed to send UserAction to Kafka: userId={}, eventId={}, error={}",
                    avro.getUserId(),
                    avro.getEventId(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("Failed to send user action to Kafka: " + e.getMessage(), e);
        }
    }

    private ActionTypeAvro toAvroActionType(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + proto);
        };
    }

    private Instant toInstant(UserActionProto proto) {
        if (proto.hasTimestamp()) {
            return Instant.ofEpochSecond(
                    proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos());
        }
        return Instant.now();
    }
}
