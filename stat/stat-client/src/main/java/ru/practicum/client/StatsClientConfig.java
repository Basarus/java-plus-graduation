package ru.practicum.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate statsRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnProperty("stats-server.url")
    public StatsClient statsClientByUrl(
            RestTemplate statsRestTemplate,
            @Value("${stats-server.url}") String baseUrl,
            @Value("${stats-server.app:ewm-main-service}") String app) {
        return new StatsClientImpl(statsRestTemplate, baseUrl, app);
    }

    @Bean
    @ConditionalOnMissingBean(StatsClient.class)
    public StatsClient statsClientByDiscovery(
            RestTemplate statsRestTemplate,
            DiscoveryClient discoveryClient,
            @Value("${stats-server.service-id:stat-server}") String serviceId,
            @Value("${stats-server.app:ewm-main-service}") String app) {
        RetryTemplate retryTemplate = buildRetryTemplate();
        return new StatsClientImpl(
                statsRestTemplate,
                () -> {
                    ServiceInstance instance =
                            retryTemplate.execute(
                                    ctx -> {
                                        List<ServiceInstance> instances =
                                                discoveryClient.getInstances(serviceId);
                                        if (instances.isEmpty()) {
                                            throw new StatsServerUnavailable(
                                                    "No instances of "
                                                            + serviceId
                                                            + " found in discovery",
                                                    new IllegalStateException(
                                                            "Empty instance list"));
                                        }
                                        return instances.getFirst();
                                    });
                    return "http://" + instance.getHost() + ":" + instance.getPort();
                },
                app);
    }

    private RetryTemplate buildRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(backOff);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
