package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateCommentDto(
        @NotBlank
        String text
) {
}
