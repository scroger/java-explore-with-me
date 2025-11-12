package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record EndpointHitDto(
        Long id,

        @NotBlank
        String app,

        @NotBlank
        String uri,

        @NotBlank
        String ip,

        @NotBlank
        String timestamp
) {
}
