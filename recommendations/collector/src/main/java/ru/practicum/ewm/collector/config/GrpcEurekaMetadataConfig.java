package ru.practicum.ewm.collector.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.netflix.appinfo.ApplicationInfoManager;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;

@Component
@RequiredArgsConstructor
public class GrpcEurekaMetadataConfig {

    private final ApplicationInfoManager applicationInfoManager;

    @EventListener
    public void onGrpcServerStarted(GrpcServerStartedEvent event) {
        String port = String.valueOf(event.getPort());

        Map<String, String> metadata =
                new HashMap<>(applicationInfoManager.getInfo().getMetadata());
        metadata.put("grpc.port", port);
        metadata.put("gRPC.port", port);
        metadata.put("grpcPort", port);
        metadata.put("gRPC__port", port);

        applicationInfoManager.registerAppMetadata(metadata);
    }
}
