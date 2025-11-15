package ru.practicum.ewm.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.EventAdminFilterDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.util.DateTimeUtil;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> adminSearch(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return eventService.adminSearch(
                new EventAdminFilterDto(
                        users,
                        states,
                        categories,
                        DateTimeUtil.parseNullable(rangeStart),
                        DateTimeUtil.parseNullable(rangeEnd)),
                PageRequest.of(from / size, size)
        );
    }

    @PatchMapping("/{eventId}")
    public EventFullDto adminUpdate(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest dto) {
        return eventService.adminUpdate(eventId, dto);
    }
}
