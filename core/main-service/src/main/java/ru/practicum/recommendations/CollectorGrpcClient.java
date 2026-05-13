package ru.practicum.recommendations;

import ru.practicum.ewm.stats.collector.proto.ActionTypeProto;
import ru.practicum.ewm.stats.collector.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.collector.proto.UserActionProto;

import org.springframework.stereotype.Component;

import com.google.protobuf.Timestamp;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Component
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void sendView(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void sendRegister(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void sendLike(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void send(long userId, long eventId, ActionTypeProto actionType) {
        try {
            long millis = System.currentTimeMillis();
            Timestamp timestamp =
                    Timestamp.newBuilder()
                            .setSeconds(millis / 1000)
                            .setNanos((int) ((millis % 1000) * 1_000_000))
                            .build();

            UserActionProto action =
                    UserActionProto.newBuilder()
                            .setUserId(userId)
                            .setEventId(eventId)
                            .setActionType(actionType)
                            .setTimestamp(timestamp)
                            .build();

            stub.collectUserAction(action);
            log.debug(
                    "Sent user action: userId={}, eventId={}, type={}",
                    userId,
                    eventId,
                    actionType);
        } catch (Exception e) {
            log.warn("Failed to send user action to collector: {}", e.getMessage());
        }
    }
}
