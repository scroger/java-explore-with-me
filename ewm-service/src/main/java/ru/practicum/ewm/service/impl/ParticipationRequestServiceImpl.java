package ru.practicum.ewm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.ParticipationRequestService;

@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (EventState.PUBLISHED != event.getState()) {
            throw new ConflictException("Cannot request participation in non‑published event");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request his own event");
        }
        if (participationRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Duplicate request");
        }
        long confirmed = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        ParticipationRequest req = ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status((0 == event.getParticipantLimit() || !event.getRequestModeration())
                        ? RequestStatus.CONFIRMED
                        : RequestStatus.PENDING)
                .build();
        ParticipationRequest saved = participationRequestRepository.save(req);

        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<ParticipationRequest> list = participationRequestRepository.findAllByRequesterId(userId);

        return list.stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event ev = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!ev.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Only initiator can view requests");
        }

        List<ParticipationRequest> list = participationRequestRepository.findAllByEventIdAndStatus(
                eventId,
                RequestStatus.PENDING);

        return list.stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest body
    ) {
        Event ev = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!ev.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Only initiator can change status");
        }
        if (!ev.getRequestModeration()) {
            throw new ConflictException("Pre‑moderation is disabled for this event");
        }

        List<ParticipationRequest> requests = participationRequestRepository.findAllById(body.requestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest pr : requests) {
            if (RequestStatus.PENDING != pr.getStatus()) {
                throw new ConflictException("Only PENDING requests can be changed");
            }

            if ("CONFIRMED".equalsIgnoreCase(body.status())) {
                long alreadyConfirmed = participationRequestRepository.countByEventIdAndStatus(
                        eventId,
                        RequestStatus.CONFIRMED);

                if (ev.getParticipantLimit() > 0 && alreadyConfirmed >= ev.getParticipantLimit()) {
                    throw new ConflictException("Participant limit reached");
                }

                pr.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(ParticipationRequestMapper.toDto(pr));
            } else if ("REJECTED".equalsIgnoreCase(body.status())) {
                pr.setStatus(RequestStatus.REJECTED);
                rejected.add(ParticipationRequestMapper.toDto(pr));
            } else {
                throw new BadRequestException("Invalid status");
            }
        }

        long confirmedNow = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (ev.getParticipantLimit() > 0 && confirmedNow >= ev.getParticipantLimit()) {
            List<ParticipationRequest> pending = participationRequestRepository.findAllByEventIdAndStatus(
                    eventId,
                    RequestStatus.PENDING);

            for (ParticipationRequest p : pending) {
                p.setStatus(RequestStatus.REJECTED);
                rejected.add(ParticipationRequestMapper.toDto(p));
            }
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest pr = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!pr.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("Cannot cancel another user's request");
        }

        if (RequestStatus.CANCELED == pr.getStatus()) {
            return ParticipationRequestMapper.toDto(pr);
        }

        pr.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(pr);
    }

}
