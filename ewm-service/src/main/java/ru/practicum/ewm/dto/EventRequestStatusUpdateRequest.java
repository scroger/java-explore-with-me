package ru.practicum.ewm.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,
        String status
) {
}
