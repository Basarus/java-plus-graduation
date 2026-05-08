package ru.practicum.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "request-service", fallback = RequestsClientFallback.class)
public interface RequestsClient {

    @GetMapping("/internal/requests/confirmed-counts")
    Map<Long, Long> getConfirmedCounts(@RequestParam("eventIds") List<Long> eventIds);
}
