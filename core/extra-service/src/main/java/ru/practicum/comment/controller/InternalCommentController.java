package ru.practicum.comment.controller;

import java.util.List;
import java.util.Map;

import ru.practicum.comment.service.CommentService;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class InternalCommentController {

    private final CommentService commentService;

    @GetMapping("/count")
    public Map<Long, Long> getCommentCounts(@RequestParam List<Long> eventIds) {
        return commentService.getCommentCountsByEventIds(eventIds);
    }
}
