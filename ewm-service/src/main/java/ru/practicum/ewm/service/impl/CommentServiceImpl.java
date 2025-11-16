package ru.practicum.ewm.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.BatchModerateDto;
import ru.practicum.ewm.dto.CommentFullDto;
import ru.practicum.ewm.dto.CreateCommentDto;
import ru.practicum.ewm.dto.ModerateCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.enums.CommentStatus;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.CommentService;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentFullDto addComment(Long userId, Long eventId, CreateCommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Comment comment = Comment.builder()
                .author(author)
                .event(event)
                .text(dto.text())
                .status(CommentStatus.PENDING)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentFullDto> getApprovedComments(Long eventId) {
        List<Comment> list = commentRepository.findAllByEventIdAndStatus(eventId, CommentStatus.APPROVED);

        return list.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentFullDto> getAllComments(Long eventId) {
        List<Comment> list = commentRepository.findAllByEventId(eventId);

        return list.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentFullDto editComment(Long userId, Long commentId, UpdateCommentDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You are not the author of this comment");
        }

        if (CommentStatus.PENDING != comment.getStatus() && CommentStatus.REJECTED != comment.getStatus()) {
            throw new ConflictException("Only comments with status PENDING or REJECTED can be edited");
        }

        comment.setText(dto.text());
        comment.setUpdated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You are not the author of this comment");
        }

        if (CommentStatus.PENDING != comment.getStatus() && CommentStatus.REJECTED != comment.getStatus()) {
            throw new ConflictException("Only comments with status PENDING or REJECTED can be deleted");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentFullDto moderateComment(Long commentId, ModerateCommentDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (CommentStatus.APPROVED == dto.status()) {
            comment.setStatus(CommentStatus.APPROVED);
        } else if (CommentStatus.REJECTED == dto.status()) {
            comment.setStatus(CommentStatus.REJECTED);
        } else {
            throw new BadRequestException("Invalid moderation status");
        }

        comment.setUpdated(LocalDateTime.now());

        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public List<CommentFullDto> batchModerate(BatchModerateDto dto) {
        List<Comment> comments = commentRepository.findAllById(dto.commentIds());

        if (comments.size() != dto.commentIds().size()) {
            throw new NotFoundException("One or more comments not found");
        }

        CommentStatus newStatus = dto.status();
        for (Comment c : comments) {
            c.setStatus(newStatus);
            c.setUpdated(LocalDateTime.now());
        }

        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

}
