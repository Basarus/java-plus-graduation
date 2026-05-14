package ru.practicum.ewm.collector.controller;

import ru.practicum.ewm.collector.service.UserActionService;
import ru.practicum.ewm.stats.collector.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.collector.proto.UserActionProto;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionGrpcController
        extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info(
                "Received user action: userId={}, eventId={}, actionType={}",
                request.getUserId(),
                request.getEventId(),
                request.getActionType());

        try {
            userActionService.collectUserAction(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(
                    "Error processing user action: userId={}, eventId={}, actionType={}",
                    request.getUserId(),
                    request.getEventId(),
                    request.getActionType(),
                    e);

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to collect user action")
                            .withCause(e)
                            .asRuntimeException());
        }
    }
}
