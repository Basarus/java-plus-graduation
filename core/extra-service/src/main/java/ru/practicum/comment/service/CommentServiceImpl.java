package ru.practicum.comment.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.practicum.client.EventsClient;
import ru.practicum.client.UsersClient;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.exception.ForbiddenAccessException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserShortDto;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UsersClient usersClient;
    private final EventsClient eventsClient;

    @Override
    @Transactional(readOnly = true)
    public Collection<CommentDto> getAllCommentsPaged(CommentsPublicGetRequest request) {
        if (eventsClient.getEventById(request.eventId()) == null) {
            throw new NotFoundException("Event with id=%d not found".formatted(request.eventId()));
        }

        Page<Comment> comments =
                commentRepository.findAllByEventId(request.eventId(), request.getPageable());

        List<Long> authorIds = comments.stream().map(Comment::getAuthorId).distinct().toList();
        Map<Long, UserShortDto> usersMap =
                usersClient.getUsersByIds(authorIds).stream()
                        .collect(Collectors.toMap(UserShortDto::id, u -> u));

        return comments.stream()
                .map(
                        c ->
                                CommentMapper.toCommentDto(
                                        c,
                                        usersMap.getOrDefault(
                                                c.getAuthorId(),
                                                new UserShortDto(c.getAuthorId(), "Unknown"))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CommentDto> getAllCommentsOfUserPaged(CommentsPrivateGetRequest request) {
        UserShortDto user = usersClient.getUserById(request.userId());
        List<Comment> comments =
                commentRepository.findAllByAuthorId(request.userId(), request.getPageable());
        return comments.stream().map(c -> CommentMapper.toCommentDto(c, user)).toList();
    }

    @Override
    public void deleteComment(long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteCommentByUser(long userId, long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenAccessException("You are not allowed to delete others comments");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto createComment(CommentsCreateRequest request) {
        if (eventsClient.getEventById(request.newComment().eventId()) == null) {
            throw new NotFoundException(
                    "Event with id=%d not found".formatted(request.newComment().eventId()));
        }
        UserShortDto user = usersClient.getUserById(request.userId());
        Comment saved =
                commentRepository.save(
                        CommentMapper.toEntity(
                                request.newComment(),
                                request.userId(),
                                request.newComment().eventId()));
        return CommentMapper.toCommentDto(saved, user);
    }

    @Override
    public CommentDto updateComment(CommentsUpdateRequest request) {
        Comment comment = getCommentOrThrow(request.commentId());
        if (!comment.getAuthorId().equals(request.userId())) {
            throw new ForbiddenAccessException("You are not allowed to update others comments");
        }
        comment.setText(request.updateComment().text());
        comment.setEdited(true);
        Comment saved = commentRepository.save(comment);
        UserShortDto user = usersClient.getUserById(request.userId());
        return CommentMapper.toCommentDto(saved, user);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getById(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        UserShortDto user = usersClient.getUserById(comment.getAuthorId());
        return CommentMapper.toCommentDto(comment, user);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getCommentCountsByEventIds(List<Long> eventIds) {
        return commentRepository.countCommentsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(r -> r.eventId(), r -> r.count()));
    }

    private Comment getCommentOrThrow(long commentId) {
        return commentRepository
                .findById(commentId)
                .orElseThrow(NotFoundException.supplier("Comment with id=%d not found", commentId));
    }
}
