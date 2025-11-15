package ru.practicum.ewm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewEventDto(
        @NotBlank
        @Size(min = 20, max = 2000)
        String annotation,

        @NotNull
        Long category,

        @NotBlank
        @Size(min = 20, max = 7000)
        String description,

        @NotBlank
        String eventDate,

        @NotNull
        LocationDto location,

        Boolean paid,

        @Min(0)
        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank
        @Size(min = 3, max = 120)
        String title
) {
}
