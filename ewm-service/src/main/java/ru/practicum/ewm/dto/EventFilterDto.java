package ru.practicum.ewm.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record EventFilterDto(
        String text,
        List<Long> categories,
        Boolean paid,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        boolean onlyAvailable,
        String sort
) {
}
