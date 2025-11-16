package ru.practicum.ewm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.practicum.ewm.enums.CommentStatus;
import ru.practicum.ewm.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "event"})
    List<Comment> findAllByEventId(Long eventId);

    @EntityGraph(attributePaths = {"author", "event"})
    List<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status);

}
