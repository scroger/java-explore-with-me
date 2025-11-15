package ru.practicum.ewm.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CompilationService;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Set<Event> events = new HashSet<>();
        if (null != dto.events() && !dto.events().isEmpty()) {
            events.addAll(eventRepository.findAllById(dto.events()));
        }

        Compilation comp = CompilationMapper.toEntity(dto, events);
        Compilation saved = compilationRepository.save(comp);
        List<EventShortDto> eventDtos = events.stream()
                .map(ev -> EventMapper.toShortDto(ev, 0L, 0L))
                .toList();

        return CompilationMapper.toDto(saved, eventDtos);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (null != dto.title()) comp.setTitle(dto.title());
        if (null != dto.pinned()) comp.setPinned(dto.pinned());
        if (null != dto.events()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.events()));
            comp.setEvents(events);
        }

        Compilation saved = compilationRepository.save(comp);
        List<EventShortDto> eventDtos = saved.getEvents().stream()
                .map(ev -> EventMapper.toShortDto(ev, 0L, 0L))
                .toList();

        return CompilationMapper.toDto(saved, eventDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        compilationRepository.delete(comp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pg = PageRequest.of(from / size, size);
        List<Compilation> list;

        if (null != pinned) {
            list = compilationRepository.findAllByPinned(pinned, pg);
        } else {
            list = compilationRepository.findAll(pg).getContent();
        }

        return list.stream()
                .map(comp -> {
                    List<EventShortDto> evDtos = comp.getEvents().stream()
                            .map(ev -> EventMapper.toShortDto(ev, 0L, 0L))
                            .toList();
                    return CompilationMapper.toDto(comp, evDtos);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        List<EventShortDto> evDtos = comp.getEvents().stream()
                .map(ev -> EventMapper.toShortDto(ev, 0L, 0L))
                .toList();

        return CompilationMapper.toDto(comp, evDtos);
    }

}
