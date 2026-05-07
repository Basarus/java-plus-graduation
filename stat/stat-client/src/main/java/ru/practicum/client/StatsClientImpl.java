package ru.practicum.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final Supplier<String> baseUrlSupplier;
    private final String app;

    public StatsClientImpl(RestTemplate restTemplate, String baseUrl, String app) {
        this.restTemplate = restTemplate;
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.baseUrlSupplier = () -> url;
        this.app = app;
    }

    public StatsClientImpl(
            RestTemplate restTemplate, Supplier<String> baseUrlSupplier, String app) {
        this.restTemplate = restTemplate;
        this.baseUrlSupplier = baseUrlSupplier;
        this.app = app;
    }

    @Override
    public void hit(EndpointHitDto dto) {
        String url = baseUrlSupplier.get() + "/hit";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> request = new HttpEntity<>(dto, headers);
        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
    }

    @Override
    public void hit(HttpServletRequest request) {
        EndpointHitDto dto =
                EndpointHitDto.builder()
                        .app(app)
                        .uri(request.getRequestURI())
                        .ip(request.getRemoteAddr())
                        .timestamp(LocalDateTime.now())
                        .build();

        hit(dto);
    }

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder b =
                UriComponentsBuilder.fromHttpUrl(baseUrlSupplier.get() + "/stats")
                        .queryParam("start", start.format(FMT))
                        .queryParam("end", end.format(FMT))
                        .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                b.queryParam("uris", uri);
            }
        }

        String url = b.build(false).toUriString();

        ResponseEntity<ViewStatsDto[]> resp = restTemplate.getForEntity(url, ViewStatsDto[].class);
        ViewStatsDto[] body = resp.getBody();
        return body == null ? List.of() : Arrays.asList(body);
    }
}
