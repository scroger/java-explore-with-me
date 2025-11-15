package ru.practicum.ewm.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record CompilationDto(
        Long id,
        String title,
        Boolean pinned,
        List<EventShortDto> events
) {
}
