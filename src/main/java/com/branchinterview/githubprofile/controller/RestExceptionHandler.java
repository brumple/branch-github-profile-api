package com.branchinterview.githubprofile.controller;

import com.branchinterview.githubprofile.client.GitHubClientException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(GitHubClientException.class)
    ResponseEntity<ErrorResponse> handleGitHubClientException(
            GitHubClientException exception, HttpServletRequest request) {
        var category = exception.category();
        return ResponseEntity
                .status(category.status())
                .body(new ErrorResponse(
                        category.code(),
                        exception.getMessage(),
                        category.status().value(),
                        request.getRequestId()));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    ResponseEntity<ErrorResponse> handleMissingPathVariable(HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "INVALID_USERNAME",
                        "Username is required.",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestId()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResourceFound(HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "NOT_FOUND",
                        "Resource was not found.",
                        HttpStatus.NOT_FOUND.value(),
                        request.getRequestId()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        request.getRequestId()));
    }
}
