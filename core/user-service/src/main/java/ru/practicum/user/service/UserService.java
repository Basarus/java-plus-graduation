package ru.practicum.user.service;

import java.util.Collection;
import java.util.List;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

public interface UserService {
    Collection<UserDto> getUsersPaged(UsersGetRequest request);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUserById(Long userId);

    UserShortDto getUserShortById(Long userId);

    List<UserShortDto> getUserShortByIds(List<Long> ids);
}
