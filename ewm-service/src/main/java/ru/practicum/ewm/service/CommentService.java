package ru.practicum.ewm.service;

import java.util.List;

import ru.practicum.ewm.dto.BatchModerateDto;
import ru.practicum.ewm.dto.CommentFullDto;
import ru.practicum.ewm.dto.ModerateCommentDto;
import ru.practicum.ewm.dto.CreateCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;

public interface CommentService {

    CommentFullDto addComment(Long userId, Long eventId, CreateCommentDto dto);

    List<CommentFullDto> getApprovedComments(Long eventId);

    List<CommentFullDto> getAllComments(Long eventId);

    CommentFullDto editComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    CommentFullDto moderateComment(Long commentId, ModerateCommentDto dto);

    List<CommentFullDto> batchModerate(BatchModerateDto dto);

}
