package ru.practicum.ewm.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ApiError(
        String status,
        String reason,
        String message,
        String timestamp,
        List<String> errors
) {
}
