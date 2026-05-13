package ru.practicum.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "compilation-service", fallback = CommentsClientFallback.class)
public interface CommentsClient {

    @GetMapping("/internal/comments/count")
    Map<Long, Long> getCommentCounts(@RequestParam("eventIds") List<Long> eventIds);
}
