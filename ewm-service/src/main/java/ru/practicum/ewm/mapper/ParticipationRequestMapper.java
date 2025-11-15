package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.util.DateTimeUtil;

public final class ParticipationRequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest pr) {
        return ParticipationRequestDto.builder()
                .id(pr.getId())
                .event(pr.getEvent().getId())
                .requester(pr.getRequester().getId())
                .status(pr.getStatus().name())
                .created(DateTimeUtil.format(pr.getCreated()))
                .build();
    }

}
