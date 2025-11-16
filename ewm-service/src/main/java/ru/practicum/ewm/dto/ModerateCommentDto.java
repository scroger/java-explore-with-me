package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.ewm.enums.CommentStatus;

@Builder
public record ModerateCommentDto(
        @NotNull
        CommentStatus status
) {
}
