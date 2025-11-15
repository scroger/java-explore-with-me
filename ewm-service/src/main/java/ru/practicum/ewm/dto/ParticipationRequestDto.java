package ru.practicum.ewm.dto;

import lombok.Builder;

@Builder
public record ParticipationRequestDto(
        Long id,
        Long event,
        Long requester,
        String status,
        String created
) {
}
