package ru.practicum.ewm.service.impl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.ewm.dto.BatchModerateDto;
import ru.practicum.ewm.dto.CommentFullDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.enums.CommentStatus;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepo;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = Comment.builder()
                .id(100L)
                .author(User.builder()
                        .id(1L)
                        .email("test@test.com")
                        .name("Author")
                        .build())
                .event(Event.builder()
                        .id(10L)
                        .title("Test event")
                        .state(EventState.PUBLISHED)
                        .build())
                .text("Test")
                .status(CommentStatus.PENDING)
                .version(0L)
                .build();
    }

    @Test
    void editComment_allowedWhenPending() {
        Mockito.when(commentRepo.findById(100L)).thenReturn(Optional.of(comment));

        UpdateCommentDto dto = UpdateCommentDto.builder()
                .text("Test new")
                .build();

        CommentFullDto result = commentService.editComment(1L, 100L, dto);
        Assertions.assertEquals("Test new", result.text());
        Assertions.assertEquals(CommentStatus.PENDING, result.status());
    }

    @Test
    void editComment_allowedWhenRejected() {
        comment.setStatus(CommentStatus.REJECTED);
        Mockito.when(commentRepo.findById(100L)).thenReturn(Optional.of(comment));

        UpdateCommentDto dto = UpdateCommentDto.builder()
                .text("Test new")
                .build();

        CommentFullDto result = commentService.editComment(1L, 100L, dto);
        Assertions.assertEquals("Test new", result.text());
        Assertions.assertEquals(CommentStatus.PENDING, result.status());
    }

    @Test
    void editComment_forbiddenIfStatusApproved() {
        comment.setStatus(CommentStatus.APPROVED);
        Mockito.when(commentRepo.findById(100L)).thenReturn(Optional.of(comment));

        UpdateCommentDto dto = UpdateCommentDto.builder()
                .text("Test new")
                .build();

        ConflictException e = Assertions.assertThrows(
                ConflictException.class,
                () -> commentService.editComment(1L, 100L, dto));

        Assertions.assertEquals("Only comments with status PENDING or REJECTED can be edited", e.getMessage());
    }

    @Test
    void deleteComment_allowedWhenPending() {
        Mockito.when(commentRepo.findById(100L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 100L);
        Mockito.verify(commentRepo, Mockito.times(1)).delete(comment);
    }

    @Test
    void deleteComment_allowedWhenRejected() {
        comment.setStatus(CommentStatus.REJECTED);
        Mockito.when(commentRepo.findById(100L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 100L);
        Mockito.verify(commentRepo, Mockito.times(1)).delete(comment);
    }

    @Test
    void deleteComment_forbiddenIfApproved() {
        comment.setStatus(CommentStatus.APPROVED);
        Mockito.when(commentRepo.findById(100L)).thenReturn(Optional.of(comment));

        ConflictException e = Assertions.assertThrows(
                ConflictException.class,
                () -> commentService.deleteComment(1L, 100L));

        Assertions.assertEquals("Only comments with status PENDING or REJECTED can be deleted", e.getMessage());
    }

    @Test
    void batchModerate_success() {
        Comment c1 = Comment.builder()
                .id(1L)
                .text("Test1")
                .status(CommentStatus.PENDING)
                .author(User.builder()
                        .id(1L)
                        .email("test@test.com")
                        .name("Author")
                        .build())
                .event(Event.builder()
                        .id(10L)
                        .title("Test event")
                        .state(EventState.PUBLISHED)
                        .build())
                .version(0L)
                .build();
        Comment c2 = Comment.builder()
                .id(2L)
                .text("Test2")
                .status(CommentStatus.PENDING)
                .author(User.builder()
                        .id(2L)
                        .email("test2@test.com")
                        .name("Author2")
                        .build())
                .event(Event.builder()
                        .id(20L)
                        .title("Test event 20")
                        .state(EventState.PUBLISHED)
                        .build())
                .version(0L)
                .build();

        Mockito.when(commentRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(c1, c2));

        BatchModerateDto dto = BatchModerateDto.builder()
                .commentIds(List.of(1L, 2L))
                .status(CommentStatus.APPROVED)
                .build();

        List<CommentFullDto> result = commentService.batchModerate(dto);
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(c -> CommentStatus.APPROVED == c.status()));
    }

}