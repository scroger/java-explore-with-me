package ru.practicum.ewm.dto;

import lombok.Builder;

@Builder
public record EventShortDto(
        Long id,
        String annotation,
        CategoryDto category,
        UserShortDto initiator,
        String title,
        String eventDate,
        Boolean paid,
        Long confirmedRequests,
        Long views
) {
}
