package ru.practicum.client;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class RequestsClientFallback implements RequestsClient {

    @Override
    public Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        return Map.of();
    }
}
