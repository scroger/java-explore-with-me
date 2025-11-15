package ru.practicum.ewm.controller;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.BatchModerateDto;
import ru.practicum.ewm.dto.CreateCommentDto;
import ru.practicum.ewm.dto.CommentFullDto;
import ru.practicum.ewm.dto.ModerateCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.service.CommentService;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullDto addComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody CreateCommentDto dto
    ) {
        return commentService.addComment(userId, eventId, dto);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentFullDto> getApproved(@PathVariable Long eventId) {
        return commentService.getApprovedComments(eventId);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentFullDto editComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto dto
    ) {
        try {
            return commentService.editComment(userId, commentId, dto);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Comment has been modified");
        }
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/admin/events/{eventId}/comments")
    public List<CommentFullDto> getAll(@PathVariable Long eventId) {
        return commentService.getAllComments(eventId);
    }

    @PatchMapping("/admin/comments/{commentId}")
    public CommentFullDto moderate(@PathVariable Long commentId, @Valid @RequestBody ModerateCommentDto dto) {
        try {
            return commentService.moderateComment(commentId, dto);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Comment has been modified");
        }
    }

    @PostMapping("/admin/comments/batch-moderate")
    public List<CommentFullDto> batchModerate(@Valid @RequestBody BatchModerateDto dto) {
        try {
            return commentService.batchModerate(dto);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Comment has been modified");
        }
    }

}
