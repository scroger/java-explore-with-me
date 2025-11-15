package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LocationDto(
        @NotNull
        Double lat,

        @NotNull
        Double lon
) {
}
