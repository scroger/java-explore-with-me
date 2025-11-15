package ru.practicum.ewm.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.EventAdminFilterDto;
import ru.practicum.ewm.dto.EventFilterDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.QEvent;
import ru.practicum.ewm.model.QParticipationRequest;
import ru.practicum.ewm.repository.EventCustomRepository;

@Repository
@RequiredArgsConstructor
public class EventCustomRepositoryImpl implements EventCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Event> findPublicEvents(EventFilterDto filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (null != filter.text() && !filter.text().isBlank()) {
            String pattern = "%" + filter.text().toLowerCase() + "%";
            builder.and(QEvent.event.annotation.lower().like(pattern)
                    .or(QEvent.event.description.lower().like(pattern)));
        }
        if (null != filter.categories() && !filter.categories().isEmpty()) {
            builder.and(QEvent.event.category.id.in(filter.categories()));
        }
        if (null != filter.paid()) {
            builder.and(QEvent.event.paid.eq(filter.paid()));
        }
        if (null != filter.rangeStart()) {
            builder.and(QEvent.event.eventDate.goe(filter.rangeStart()));
        }
        if (null != filter.rangeEnd()) {
            builder.and(QEvent.event.eventDate.loe(filter.rangeEnd()));
        }
        if (filter.onlyAvailable()) {
            var sub = JPAExpressions.select(QParticipationRequest.participationRequest.count())
                    .from(QParticipationRequest.participationRequest)
                    .where(QParticipationRequest.participationRequest.event.id.eq(QEvent.event.id)
                            .and(QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED)));

            builder.and(
                    QEvent.event.participantLimit.eq(0)
                            .or(sub.lt(QEvent.event.participantLimit.longValue()))
            );
        }

        builder.and(QEvent.event.state.eq(EventState.PUBLISHED));

        Order order = pageable.getSort().isSorted()
                ? (null != pageable.getSort().getOrderFor("views")
                ? Order.DESC
                : Order.ASC)
                : Order.ASC;

        return jpaQueryFactory.selectFrom(QEvent.event)
                .where(builder)
                .orderBy(Order.ASC == order
                        ? QEvent.event.eventDate.asc()
                        : QEvent.event.eventDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long countPublicEvents(EventFilterDto filter) {
        BooleanBuilder builder = new BooleanBuilder();

        if (null != filter.text() && !filter.text().isBlank()) {
            String pattern = "%" + filter.text().toLowerCase() + "%";
            builder.and(QEvent.event.annotation.lower().like(pattern)
                    .or(QEvent.event.description.lower().like(pattern)));
        }
        if (null != filter.categories() && !filter.categories().isEmpty()) {
            builder.and(QEvent.event.category.id.in(filter.categories()));
        }
        if (null != filter.paid()) {
            builder.and(QEvent.event.paid.eq(filter.paid()));
        }
        if (null != filter.rangeStart()) {
            builder.and(QEvent.event.eventDate.goe(filter.rangeStart()));
        }
        if (null != filter.rangeEnd()) {
            builder.and(QEvent.event.eventDate.loe(filter.rangeEnd()));
        }
        if (filter.onlyAvailable()) {
            var sub = JPAExpressions.select(QParticipationRequest.participationRequest.count())
                    .from(QParticipationRequest.participationRequest)
                    .where(QParticipationRequest.participationRequest.event.id.eq(QEvent.event.id)
                            .and(QParticipationRequest.participationRequest.status.eq(RequestStatus.CONFIRMED)));
            builder.and(
                    QEvent.event.participantLimit.eq(0)
                            .or(sub.lt(QEvent.event.participantLimit.longValue()))
            );
        }
        builder.and(QEvent.event.state.eq(EventState.PUBLISHED));

        return jpaQueryFactory.selectFrom(QEvent.event).where(builder).fetchCount();
    }

    @Override
    public Optional<Event> findByIdAndState(Long id, EventState state) {
        Event ev = jpaQueryFactory.selectFrom(QEvent.event)
                .where(QEvent.event.id.eq(id).and(QEvent.event.state.eq(state)))
                .fetchOne();
        return Optional.ofNullable(ev);
    }

    @Override
    public List<Event> adminSearch(EventAdminFilterDto filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (null != filter.users() && !filter.users().isEmpty()) {
            builder.and(QEvent.event.initiator.id.in(filter.users()));
        }
        if (null != filter.states() && !filter.states().isEmpty()) {
            builder.and(QEvent.event.state.in(filter.states().stream()
                    .map(EventState::valueOf)
                    .toList()));
        }
        if (null != filter.categories() && !filter.categories().isEmpty()) {
            builder.and(QEvent.event.category.id.in(filter.categories()));
        }
        if (null != filter.rangeStart()) {
            builder.and(QEvent.event.eventDate.goe(filter.rangeStart()));
        }
        if (null != filter.rangeEnd()) {
            builder.and(QEvent.event.eventDate.loe(filter.rangeEnd()));
        }

        return jpaQueryFactory.selectFrom(QEvent.event)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long adminCount(EventAdminFilterDto filter) {
        BooleanBuilder builder = new BooleanBuilder();

        if (null != filter.users() && !filter.users().isEmpty()) {
            builder.and(QEvent.event.initiator.id.in(filter.users()));
        }
        if (null != filter.states() && !filter.states().isEmpty()) {
            builder.and(QEvent.event.state.in(filter.states().stream()
                    .map(EventState::valueOf)
                    .toList()));
        }
        if (null != filter.categories() && !filter.categories().isEmpty()) {
            builder.and(QEvent.event.category.id.in(filter.categories()));
        }
        if (null != filter.rangeStart()) {
            builder.and(QEvent.event.eventDate.goe(filter.rangeStart()));
        }
        if (null != filter.rangeEnd()) {
            builder.and(QEvent.event.eventDate.loe(filter.rangeEnd()));
        }

        return jpaQueryFactory.selectFrom(QEvent.event).where(builder).fetchCount();
    }
}
