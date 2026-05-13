package ru.practicum.recommendations;

import java.util.ArrayList;
import java.util.List;

import ru.practicum.ewm.stats.analyzer.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.analyzer.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.analyzer.proto.UserPredictionsRequestProto;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Component
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    public List<Long> getRecommendationsForUser(long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request =
                    UserPredictionsRequestProto.newBuilder()
                            .setUserId(userId)
                            .setMaxResults(maxResults)
                            .build();
            List<Long> ids = new ArrayList<>();
            stub.getRecommendationsForUser(request).forEachRemaining(r -> ids.add(r.getEventId()));
            return ids;
        } catch (Exception e) {
            log.warn("Failed to get recommendations for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    public List<Long> getSimilarEvents(long eventId, long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request =
                    SimilarEventsRequestProto.newBuilder()
                            .setEventId(eventId)
                            .setUserId(userId)
                            .setMaxResults(maxResults)
                            .build();
            List<Long> ids = new ArrayList<>();
            stub.getSimilarEvents(request).forEachRemaining(r -> ids.add(r.getEventId()));
            return ids;
        } catch (Exception e) {
            log.warn("Failed to get similar events for eventId {}: {}", eventId, e.getMessage());
            return List.of();
        }
    }
}
