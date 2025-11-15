package ru.practicum.stats.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.repository.EndpointHitRepository;
import ru.practicum.stats.service.StatsService;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EndpointHitRepository repository;

    @Transactional
    public void save(EndpointHitDto dto) {
        repository.save(EndpointHit.builder()
                .app(dto.app())
                .uri(dto.uri())
                .ip(dto.ip())
                .timestamp(LocalDateTime.parse(dto.timestamp(), FORMATTER))
                .build());
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date should be before end date");
        }

        if (unique) {
            return repository.findUniqueStats(start, end, uris);
        }

        return repository.findStats(start, end, uris);
    }

}
