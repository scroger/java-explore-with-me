package ru.practicum.ewm.exception;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.dto.ApiError;
import ru.practicum.ewm.util.DateTimeUtil;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private ApiError build(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(DateTimeUtil.format(LocalDateTime.now()))
                .errors(null == errors ? Collections.emptyList() : errors)
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return new ResponseEntity<>(build(HttpStatus.NOT_FOUND,
                "The required object was not found.", ex.getMessage(), null),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex) {
        return new ResponseEntity<>(build(HttpStatus.CONFLICT,
                "Integrity constraint has been violated.", ex.getMessage(), null),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> forbidden(ForbiddenException ex) {
        return new ResponseEntity<>(build(HttpStatus.FORBIDDEN,
                "For the requested operation the conditions are not met.", ex.getMessage(), null),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException ex) {
        return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.", ex.getMessage(), null),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validationError(MethodArgumentNotValidException ex) {
        List<String> errs = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> "Field: " + e.getField() + ". Error: " + e.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(build(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.", String.join("; ", errs), errs),
                HttpStatus.BAD_REQUEST);
    }
}
