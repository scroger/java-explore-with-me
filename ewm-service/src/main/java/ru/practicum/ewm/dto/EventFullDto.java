package ru.practicum.ewm.dto;

import lombok.Builder;

@Builder
public record EventFullDto(
        Long id,
        String annotation,
        String description,
        CategoryDto category,
        UserShortDto initiator,
        LocationDto location,
        String title,
        String eventDate,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        String state,
        Long confirmedRequests,
        Long views,
        String createdOn,
        String publishedOn
) {
}
