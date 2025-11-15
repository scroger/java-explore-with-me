package ru.practicum.ewm.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewCompilationDto(
        @NotBlank
        @Size(min = 1, max = 50)
        String title,

        boolean pinned,
        List<Long> events
) {
}
