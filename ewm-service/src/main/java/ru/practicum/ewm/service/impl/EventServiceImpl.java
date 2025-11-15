package ru.practicum.ewm.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.dto.EventAdminFilterDto;
import ru.practicum.ewm.dto.EventFilterDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.dto.UpdateEventUserRequest;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.QEvent;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventCustomRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    public static final String APP = "ewm";

    private final EventRepository eventRepository;
    private final EventCustomRepository eventCustomRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EntityManager entityManager;
    private final StatsClient statsClient;

    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(EventFilterDto filter, Pageable pageable, HttpServletRequest httpReq) {
        statsClient.hit(APP, "/events", httpReq.getRemoteAddr());

        if (filter.rangeStart() != null && filter.rangeEnd() != null
                && filter.rangeStart().isAfter(filter.rangeEnd())) {
            throw new BadRequestException("rangeStart should be before rangeEnd");
        }

        List<Event> events = eventCustomRepository.findPublicEvents(filter, pageable);

        List<Long> ids = events.stream().map(Event::getId).toList();
        List<ViewStatsDto> viewStats = statsClient.getStats(
                events.isEmpty() ? LocalDateTime.now() : events.getFirst().getCreatedOn(),
                LocalDateTime.now(),
                ids.stream().map(id -> "/events/" + id).toList(),
                false);

        Map<Long, Long> viewsMap = viewStats.stream()
                .collect(Collectors.toMap(v -> Long.parseLong(v.uri().replace("/events/", "")),
                        ViewStatsDto::hits));

        Map<Long, Long> confirmedMap = participationRequestRepository.findAllById(ids).stream()
                .filter(r -> RequestStatus.CONFIRMED == r.getStatus())
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(),
                        Collectors.counting()));

        return events.stream()
                .map(event -> EventMapper.toShortDto(
                        event,
                        viewsMap.getOrDefault(event.getId(), 0L),
                        confirmedMap.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long id, HttpServletRequest httpReq) {
        statsClient.hit(APP, "/events/" + id, httpReq.getRemoteAddr());

        Event event = eventCustomRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Long views = statsClient.getStats(
                        event.getCreatedOn(),
                        LocalDateTime.now(),
                        List.of("/events/" + id),
                        true)
                .stream()
                .findFirst()
                .map(ViewStatsDto::hits)
                .orElse(0L);

        long confirmed = participationRequestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        return EventMapper.toFullDto(event, views, confirmed);
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Category category = categoryRepository.findById(dto.category())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        LocalDateTime eventDt = DateTimeUtil.parse(dto.eventDate());
        if (eventDt.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours in the future");
        }

        Event event = EventMapper.toEntity(dto, user, category);
        Event saved = eventRepository.save(event);

        return EventMapper.toFullDto(saved, 0L, 0L);
    }

    @Transactional
    public EventFullDto updateOwnEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can modify the event");
        }

        if (EventState.PENDING != event.getState() && EventState.CANCELED != event.getState()) {
            throw new ConflictException("Only pending or cancelled events can be changed");
        }

        if (null != dto.annotation()) event.setAnnotation(dto.annotation());
        if (null != dto.description()) event.setDescription(dto.description());
        if (null != dto.title()) event.setTitle(dto.title());

        if (null != dto.eventDate()) {
            LocalDateTime nd = DateTimeUtil.parse(dto.eventDate());
            if (nd.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Event date must be at least 2 hours in the future");
            }
            event.setEventDate(nd);
        }

        if (null != dto.category()) {
            Category cat = categoryRepository.findById(dto.category())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(cat);
        }

        if (null != dto.location()) {
            event.setLat(dto.location().lat());
            event.setLon(dto.location().lon());
        }

        if (null != dto.paid()) event.setPaid(dto.paid());
        if (null != dto.participantLimit()) event.setParticipantLimit(dto.participantLimit());
        if (null != dto.requestModeration()) event.setRequestModeration(dto.requestModeration());

        if (null != dto.stateAction()) {
            switch (dto.stateAction()) {
                case "CANCEL_REVIEW" -> event.setState(EventState.CANCELED);
                case "SEND_TO_REVIEW" -> event.setState(EventState.PENDING);
                default -> throw new BadRequestException("Invalid stateAction");
            }
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getMyEvents(Long userId, Pageable pageable) {
        QEvent e = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(e.initiator.id.eq(userId));

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        List<Event> events = qf.selectFrom(e)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> ids = events.stream().map(Event::getId).toList();
        List<ViewStatsDto> viewStats = statsClient.getStats(
                events.getFirst().getCreatedOn(),
                LocalDateTime.now(),
                ids.stream().map(id -> "/events/" + id).toList(),
                false);
        Map<Long, Long> viewsMap = viewStats.stream()
                .collect(Collectors.toMap(v -> Long.parseLong(v.uri().replace("/events/", "")),
                        ViewStatsDto::hits));
        Map<Long, Long> confirmedMap = participationRequestRepository.findAllById(ids).stream()
                .filter(r -> RequestStatus.CONFIRMED == r.getStatus())
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(),
                        Collectors.counting()));

        return events.stream()
                .map(ev -> EventMapper.toShortDto(
                        ev,
                        viewsMap.getOrDefault(ev.getId(), 0L),
                        confirmedMap.getOrDefault(ev.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public EventFullDto getOwnEvent(Long userId, Long eventId) {
        Event ev = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!ev.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }

        long confirmed = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        return EventMapper.toFullDto(ev, 0L, confirmed);
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> adminSearch(EventAdminFilterDto filter, Pageable pageable) {
        List<Event> events = eventCustomRepository.adminSearch(filter, pageable);

        List<Long> ids = events.stream().map(Event::getId).toList();
        List<ViewStatsDto> viewStats = statsClient.getStats(
                events.getFirst().getCreatedOn(),
                LocalDateTime.now(),
                ids.stream().map(id -> "/events/" + id).toList(),
                false);
        Map<Long, Long> viewsMap = viewStats.stream()
                .collect(Collectors.toMap(v -> Long.parseLong(v.uri().replace("/events/", "")),
                        ViewStatsDto::hits));
        Map<Long, Long> confirmedMap = participationRequestRepository.findAllByEventIdIn(ids).stream()
                .filter(r -> RequestStatus.CONFIRMED == r.getStatus())
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(),
                        Collectors.counting()));

        return events.stream()
                .map(ev -> EventMapper.toFullDto(
                        ev,
                        viewsMap.getOrDefault(ev.getId(), 0L),
                        confirmedMap.getOrDefault(ev.getId(), 0L)))
                .toList();
    }

    @Transactional
    public EventFullDto adminUpdate(Long eventId, UpdateEventAdminRequest dto) {
        Event ev = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (null != dto.stateAction()) {
            switch (dto.stateAction()) {
                case "PUBLISH_EVENT" -> {
                    if (EventState.PENDING != ev.getState()) {
                        throw new ConflictException("Can publish only PENDING events");
                    }
                    ev.setState(EventState.PUBLISHED);
                    ev.setPublishedOn(LocalDateTime.now());
                }
                case "REJECT_EVENT" -> {
                    if (EventState.PUBLISHED == ev.getState()) {
                        throw new ConflictException("Cannot reject already PUBLISHED event");
                    }
                    ev.setState(EventState.CANCELED);
                }
                default -> throw new BadRequestException("Invalid stateAction");
            }
        }

        if (null != dto.annotation()) ev.setAnnotation(dto.annotation());
        if (null != dto.description()) ev.setDescription(dto.description());
        if (null != dto.title()) ev.setTitle(dto.title());

        if (null != dto.eventDate()) {
            LocalDateTime nd = DateTimeUtil.parse(dto.eventDate());
            if (nd.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Event date cannot be earlier than 1 hour before publication");
            }
            ev.setEventDate(nd);
        }

        if (null != dto.category()) {
            Category cat = categoryRepository.findById(dto.category())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            ev.setCategory(cat);
        }

        if (null != dto.location()) {
            ev.setLat(dto.location().lat());
            ev.setLon(dto.location().lon());
        }

        if (null != dto.paid()) ev.setPaid(dto.paid());
        if (null != dto.participantLimit()) ev.setParticipantLimit(dto.participantLimit());
        if (null != dto.requestModeration()) ev.setRequestModeration(dto.requestModeration());

        Event saved = eventRepository.save(ev);
        return EventMapper.toFullDto(saved, 0L, 0L);
    }

}
