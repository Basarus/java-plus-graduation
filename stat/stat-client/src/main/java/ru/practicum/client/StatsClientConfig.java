package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate statsRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(
            RestTemplate statsRestTemplate,
            @Value("${stats-server.url}") String baseUrl,
            @Value("${stats-server.app}") String app) {
        return new StatsClientImpl(statsRestTemplate, baseUrl, app);
    }
}
