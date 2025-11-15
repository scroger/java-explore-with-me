package ru.practicum.ewm.dto;

import lombok.Builder;

@Builder
public record UserShortDto(
        Long id,
        String name
) {
}
