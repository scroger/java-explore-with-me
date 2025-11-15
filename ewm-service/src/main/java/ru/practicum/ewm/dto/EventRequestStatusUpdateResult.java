package ru.practicum.ewm.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record EventRequestStatusUpdateResult(
        List<ParticipationRequestDto> confirmedRequests,
        List<ParticipationRequestDto> rejectedRequests
) {
}
