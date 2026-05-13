package ru.practicum.exception.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenAccessException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.dto.ApiError;
import ru.practicum.exception.dto.Violation;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        log.error(e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return new ApiError(
                List.of(sw.toString()),
                "An error occured while processing request",
                "Exception",
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ApiError handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.warn(e.getMessage(), e);
        return new ApiError(
                null,
                e.getMessage(),
                "Some fields of request are invalid",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
    })
    public ApiError handleRequestParamException(Exception e) {
        log.warn(e.getMessage(), e);
        return new ApiError(
                null,
                e.getMessage(),
                "Incorrect request",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiError handleConstraintValidationException(ConstraintViolationException e) {
        final List<Violation> violations =
                e.getConstraintViolations().stream()
                        .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
                        .collect(Collectors.toList());
        log.warn(violations.toString());
        return new ApiError(
                violations.stream().map(Violation::toString).toList(),
                e.getMessage(),
                "Some fields of RequestBody for request are invalid",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final List<Violation> violations =
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.toList());
        log.warn(violations.toString());
        return new ApiError(
                violations.stream().map(Violation::toString).toList(),
                e.getMessage(),
                "Some fields of RequestBody for request are invalid",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.warn("Not found: {}", e.getMessage());
        return new ApiError(
                null,
                e.getMessage(),
                "The required object was not found",
                HttpStatus.NOT_FOUND.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ApiError handleConflictException(Exception e) {
        log.warn(e.getMessage(), e);
        return new ApiError(
                null,
                e.getMessage(),
                "Conflict",
                HttpStatus.CONFLICT.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenAccessException.class)
    public ApiError handleForbiddenAccessException(ForbiddenAccessException e) {
        log.warn(e.getMessage(), e);
        return new ApiError(
                null,
                e.getMessage(),
                "Forbidden",
                HttpStatus.FORBIDDEN.toString(),
                LocalDateTime.now());
    }
}
