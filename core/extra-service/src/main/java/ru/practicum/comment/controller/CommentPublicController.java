package ru.practicum.comment.controller;

import java.util.Collection;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.service.CommentsPublicGetRequest;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class CommentPublicController {

    private final CommentService commentService;

    @GetMapping
    public Collection<CommentDto> getCommentsByEvent(
            @PathVariable long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Get comments for eventId={} requested", eventId);
        CommentsPublicGetRequest request = new CommentsPublicGetRequest(eventId, from, size);
        return commentService.getAllCommentsPaged(request);
    }
}
