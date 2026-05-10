package ru.practicum.comment.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.practicum.comment.dto.CommentDto;

public interface CommentService {

    Collection<CommentDto> getAllCommentsPaged(CommentsPublicGetRequest request);

    Collection<CommentDto> getAllCommentsOfUserPaged(CommentsPrivateGetRequest request);

    void deleteComment(long commentId);

    void deleteCommentByUser(long userId, long commentId);

    CommentDto createComment(CommentsCreateRequest request);

    CommentDto updateComment(CommentsUpdateRequest request);

    CommentDto getById(Long commentId);

    Map<Long, Long> getCommentCountsByEventIds(List<Long> eventIds);
}
