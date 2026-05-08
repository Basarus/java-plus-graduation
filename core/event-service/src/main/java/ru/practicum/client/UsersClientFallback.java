package ru.practicum.client;

import java.util.List;

import ru.practicum.user.dto.UserShortDto;

import org.springframework.stereotype.Component;

@Component
public class UsersClientFallback implements UsersClient {

    @Override
    public UserShortDto getUserById(Long id) {
        return new UserShortDto(id, "Unknown");
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return ids.stream().map(id -> new UserShortDto(id, "Unknown")).toList();
    }
}
