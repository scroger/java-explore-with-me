package ru.practicum.ewm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import ru.practicum.ewm.dto.EventAdminFilterDto;
import ru.practicum.ewm.dto.EventFilterDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.model.Event;

public interface EventCustomRepository {
    List<Event> findPublicEvents(EventFilterDto filter, Pageable pageable);

    long countPublicEvents(EventFilterDto filter);

    Optional<Event> findByIdAndState(Long id, EventState state);

    List<Event> adminSearch(EventAdminFilterDto filter, Pageable pageable);

    long adminCount(EventAdminFilterDto filter);
}