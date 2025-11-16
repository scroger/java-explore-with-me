package ru.practicum.ewm.dto;

import lombok.Builder;
import ru.practicum.ewm.enums.CommentStatus;

@Builder
public record CommentFullDto(
        Long id,
        Long eventId,
        UserShortDto author,
        String text,
        CommentStatus status,
        String created,
        String updated
) {
}
