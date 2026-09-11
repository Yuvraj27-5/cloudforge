package com.cloudforge.backend.common.exception;

import com.cloudforge.backend.deployment.InvalidStatusTransitionException;
import com.cloudforge.backend.risk.DeploymentNotApprovedException;
import com.cloudforge.backend.risk.MetricsIncompleteException;
import com.cloudforge.backend.risk.MetricsMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * RFC 9457 Problem Details for every error path. See docs/api-conventions.md.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_BASE = "https://cloudforge.dev/errors/";

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE + "not-found"));
        problem.setTitle("Resource not found");
        return problem;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE + "conflict"));
        problem.setTitle("Resource already exists");
        return problem;
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    ProblemDetail handleInvalidTransition(InvalidStatusTransitionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE + "invalid-transition"));
        problem.setTitle("Invalid status transition");
        return problem;
    }

    /** 409: the deployment exists and the request is valid, but policy forbids it. */
    @ExceptionHandler(DeploymentNotApprovedException.class)
    ProblemDetail handleNotApproved(DeploymentNotApprovedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE + "not-approved"));
        problem.setTitle("Deployment not approved");
        return problem;
    }

    /** 422: well-formed request, but there is nothing measured to score. */
    @ExceptionHandler({MetricsMissingException.class, MetricsIncompleteException.class})
    ProblemDetail handleMetrics(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create(ERROR_BASE + "metrics-unavailable"));
        problem.setTitle("Cannot score this deployment");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", String.valueOf(fieldError.getDefaultMessage())))
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request contains invalid fields");
        problem.setType(URI.create(ERROR_BASE + "validation"));
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Catch-all. The stack trace is logged server-side; the client gets a generic
     * message. Without this, an unhandled exception leaks class names, SQL fragments
     * and file paths to whoever called the API.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setType(URI.create(ERROR_BASE + "internal"));
        problem.setTitle("Internal server error");
        return problem;
    }
}
