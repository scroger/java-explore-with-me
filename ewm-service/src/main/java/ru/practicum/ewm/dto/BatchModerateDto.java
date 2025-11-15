package ru.practicum.ewm.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.ewm.enums.CommentStatus;

@Builder
public record BatchModerateDto(
        @NotEmpty
        List<@NotNull Long> commentIds,

        @NotNull
        CommentStatus status
) {
}
