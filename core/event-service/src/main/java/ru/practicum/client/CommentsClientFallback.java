package ru.practicum.client;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CommentsClientFallback implements CommentsClient {

    @Override
    public Map<Long, Long> getCommentCounts(List<Long> eventIds) {
        return Map.of();
    }
}
