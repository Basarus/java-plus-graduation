package ru.practicum.ewm.analyzer.controller;

import java.util.List;

import ru.practicum.ewm.analyzer.service.RecommendationService;
import ru.practicum.ewm.analyzer.service.ScoredEvent;
import ru.practicum.ewm.stats.analyzer.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.analyzer.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.analyzer.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.analyzer.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.analyzer.proto.UserPredictionsRequestProto;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcController
        extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        log.info(
                "GetRecommendationsForUser: userId={}, maxResults={}",
                request.getUserId(),
                request.getMaxResults());
        try {
            List<ScoredEvent> results =
                    recommendationService.getRecommendationsForUser(
                            request.getUserId(), request.getMaxResults());
            for (ScoredEvent se : results) {
                responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(se.eventId())
                                .setScore(se.score())
                                .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in GetRecommendationsForUser", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        log.info(
                "GetSimilarEvents: eventId={}, userId={}, maxResults={}",
                request.getEventId(),
                request.getUserId(),
                request.getMaxResults());
        try {
            List<ScoredEvent> results =
                    recommendationService.getSimilarEvents(
                            request.getEventId(), request.getUserId(), request.getMaxResults());
            for (ScoredEvent se : results) {
                responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(se.eventId())
                                .setScore(se.score())
                                .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in GetSimilarEvents", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver) {
        log.error(
                "GET INTERACTIONS COUNT CONTROLLER CALLED: eventIds={}", request.getEventIdList());

        try {
            List<ScoredEvent> results =
                    recommendationService.getInteractionsCount(request.getEventIdList());

            for (ScoredEvent se : results) {
                log.error(
                        "SEND INTERACTIONS COUNT RESPONSE: eventId={}, score={}",
                        se.eventId(),
                        se.score());

                responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(se.eventId())
                                .setScore(se.score())
                                .build());
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in GetInteractionsCount", e);
            responseObserver.onError(e);
        }
    }
}
