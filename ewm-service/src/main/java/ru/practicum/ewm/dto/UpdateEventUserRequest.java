package ru.practicum.ewm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateEventUserRequest(
        @Size(min = 20, max = 2000)
        String annotation,

        Long category,

        @Size(min = 20, max = 7000)
        String description,

        String eventDate,
        LocationDto location,
        Boolean paid,

        @Min(0)
        Integer participantLimit,

        Boolean requestModeration,
        String stateAction,

        @Size(min = 3, max = 120)
        String title
) {
}
