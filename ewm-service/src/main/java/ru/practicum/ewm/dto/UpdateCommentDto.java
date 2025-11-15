package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateCommentDto(
        @NotBlank
        String text
) {
}
