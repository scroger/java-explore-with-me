package ru.practicum.ewm.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record EventAdminFilterDto(
        List<Long> users,
        List<String> states,
        List<Long> categories,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd
) {
}
