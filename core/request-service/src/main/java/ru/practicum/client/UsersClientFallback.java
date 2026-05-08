package ru.practicum.client;

import ru.practicum.user.dto.UserShortDto;

import org.springframework.stereotype.Component;

@Component
public class UsersClientFallback implements UsersClient {

    @Override
    public UserShortDto getUserById(Long id) {
        return null;
    }
}
