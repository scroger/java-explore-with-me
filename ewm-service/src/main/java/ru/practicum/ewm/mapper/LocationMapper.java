package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.LocationDto;

@UtilityClass
public final class LocationMapper {
    public static LocationDto toDto(Double lat, Double lon) {
        return LocationDto.builder()
                .lat(lat)
                .lon(lon)
                .build();
    }
}
