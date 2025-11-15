package ru.practicum.ewm.mapper;

import java.util.Optional;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.util.DateTimeUtil;

@UtilityClass
public final class EventMapper {

    public static EventShortDto toShortDto(Event event, long views, long confirmed) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .title(event.getTitle())
                .eventDate(DateTimeUtil.format(event.getEventDate()))
                .paid(event.isPaid())
                .confirmedRequests(confirmed)
                .views(views)
                .build();
    }

    public static EventFullDto toFullDto(Event ev, long views, long confirmed) {
        return EventFullDto.builder()
                .id(ev.getId())
                .annotation(ev.getAnnotation())
                .description(ev.getDescription())
                .category(CategoryMapper.toDto(ev.getCategory()))
                .initiator(UserMapper.toShortDto(ev.getInitiator()))
                .location(LocationMapper.toDto(ev.getLat(), ev.getLon()))
                .title(ev.getTitle())
                .eventDate(DateTimeUtil.format(ev.getEventDate()))
                .paid(ev.isPaid())
                .participantLimit(ev.getParticipantLimit())
                .requestModeration(ev.getRequestModeration())
                .state(ev.getState().name())
                .createdOn(DateTimeUtil.format(ev.getCreatedOn()))
                .publishedOn(null != ev.getPublishedOn() ? DateTimeUtil.format(ev.getPublishedOn()) : null)
                .confirmedRequests(confirmed)
                .views(views)
                .build();
    }

    public static Event toEntity(NewEventDto dto, User initiator, Category category) {
        return Event.builder()
                .annotation(dto.annotation())
                .description(dto.description())
                .title(dto.title())
                .eventDate(DateTimeUtil.parse(dto.eventDate()))
                .paid(Optional.ofNullable(dto.paid()).orElse(false))
                .participantLimit(Optional.ofNullable(dto.participantLimit()).orElse(0))
                .requestModeration(Optional.ofNullable(dto.requestModeration()).orElse(true))
                .category(category)
                .initiator(initiator)
                .lat(dto.location().lat())
                .lon(dto.location().lon())
                .state(EventState.PENDING)
                .build();
    }
}
