package ru.practicum.user.controller;

import java.util.List;

import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserShortDto getUserById(@PathVariable Long id) {
        return userService.getUserShortById(id);
    }

    @GetMapping
    public List<UserShortDto> getUsersByIds(@RequestParam List<Long> ids) {
        return userService.getUserShortByIds(ids);
    }
}
