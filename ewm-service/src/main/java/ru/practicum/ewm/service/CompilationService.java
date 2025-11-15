package ru.practicum.ewm.service;

import java.util.List;

import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto);

    void deleteCompilation(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long compId);
}
