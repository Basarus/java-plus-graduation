package ru.practicum.recommendations;

import java.util.List;

import jakarta.validation.constraints.Positive;

import ru.practicum.event.dto.EventShortDto;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CollectorGrpcClient collectorGrpcClient;

    @GetMapping("/users/{userId}/recommendations")
    public List<EventShortDto> getRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") @Positive int maxResults) {
        log.info("Get recommendations for userId={}, maxResults={}", userId, maxResults);
        return recommendationService.getRecommendationsForUser(userId, maxResults);
    }

    @GetMapping("/events/{eventId}/similar")
    public List<EventShortDto> getSimilarEvents(
            @PathVariable Long eventId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") @Positive int maxResults) {
        log.info(
                "Get similar events for eventId={}, userId={}, maxResults={}",
                eventId,
                userId,
                maxResults);
        return recommendationService.getSimilarEvents(eventId, userId, maxResults);
    }

    @PutMapping("/users/{userId}/events/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Like event: userId={}, eventId={}", userId, eventId);
        collectorGrpcClient.sendLike(userId, eventId);
    }
}
