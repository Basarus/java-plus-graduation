package ru.practicum.comment.mapper;

import java.time.LocalDateTime;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.user.dto.UserShortDto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment, UserShortDto author) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                author,
                comment.getCreated(),
                comment.isEdited());
    }

    public Comment toEntity(NewCommentDto dto, Long authorId, Long eventId) {
        return new Comment(null, dto.text(), authorId, eventId, LocalDateTime.now(), false);
    }
}
