package ru.practicum.ewm.dto;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateCompilationRequest(
        @Size(min = 1, max = 50)
        String title,

        Boolean pinned,
        List<Long> events
) {
}
