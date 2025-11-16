package ru.practicum.ewm.mapper;

import java.util.List;
import java.util.Set;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

@UtilityClass
public final class CompilationMapper {

    public static CompilationDto toDto(Compilation comp, List<EventShortDto> eventDtos) {
        return CompilationDto.builder()
                .id(comp.getId())
                .title(comp.getTitle())
                .pinned(comp.getPinned())
                .events(eventDtos)
                .build();
    }

    public static Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .title(dto.title())
                .pinned(dto.pinned())
                .events(events)
                .build();
    }

}
