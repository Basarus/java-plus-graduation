package ru.practicum.client;

import ru.practicum.user.dto.UserShortDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallback = UsersClientFallback.class)
public interface UsersClient {

    @GetMapping("/internal/users/{id}")
    UserShortDto getUserById(@PathVariable Long id);
}
