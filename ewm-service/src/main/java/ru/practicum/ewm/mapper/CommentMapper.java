package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CommentFullDto;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.util.DateTimeUtil;

@UtilityClass
public final class CommentMapper {

    public static CommentFullDto toDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .author(UserMapper.toShortDto(comment.getAuthor()))
                .text(comment.getText())
                .status(comment.getStatus())
                .created(DateTimeUtil.format(comment.getCreated()))
                .updated(null != comment.getUpdated() ? DateTimeUtil.format(comment.getUpdated()) : null)
                .build();
    }

}
