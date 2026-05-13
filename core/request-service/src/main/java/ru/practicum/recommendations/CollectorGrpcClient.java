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

    public void sendRegister(long userId, long eventId) {
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
                            .setActionType(ActionTypeProto.ACTION_REGISTER)
                            .setTimestamp(timestamp)
                            .build();

            stub.collectUserAction(action);
            log.debug("Sent REGISTER action: userId={}, eventId={}", userId, eventId);
        } catch (Exception e) {
            log.warn("Failed to send REGISTER action to collector: {}", e.getMessage());
        }
    }
}
