package ru.practicum.ewm.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.EventAdminFilterDto;
import ru.practicum.ewm.dto.EventFilterDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.UpdateEventUserRequest;

public interface EventService {

    List<EventShortDto> getPublicEvents(EventFilterDto filter, Pageable pageable, HttpServletRequest httpReq);

    EventFullDto getPublicEvent(Long id, HttpServletRequest httpReq);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    EventFullDto updateOwnEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventShortDto> getMyEvents(Long userId, Pageable pageable);

    EventFullDto getOwnEvent(Long userId, Long eventId);

    List<EventFullDto> adminSearch(EventAdminFilterDto filter, Pageable pageable);

    EventFullDto adminUpdate(Long eventId, UpdateEventAdminRequest dto);

}
