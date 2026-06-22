package com.adnane.moulcyber.api.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.adnane.moulcyber.application.auth.EmailAlreadyUsedException;
import com.adnane.moulcyber.application.auth.InvalidCredentialsException;
import com.adnane.moulcyber.application.catalog.GameNotFoundException;
import com.adnane.moulcyber.application.rental.GameUnavailableException;
import com.adnane.moulcyber.application.rental.RentalNotFoundException;
import com.adnane.moulcyber.application.rental.RentalItemAlreadyProcessedException;
import com.adnane.moulcyber.application.rental.RentalItemNotFoundException;
import com.adnane.moulcyber.application.user.UserNotFoundException;
import com.adnane.moulcyber.application.review.DuplicateReviewException;
import com.adnane.moulcyber.application.review.ReviewEligibilityException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    ResponseEntity<ApiError> handleEmailAlreadyUsed(
            EmailAlreadyUsedException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({InvalidCredentialsException.class, UserNotFoundException.class})
    ResponseEntity<ApiError> handleUnauthorized(
            RuntimeException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(GameNotFoundException.class)
    ResponseEntity<ApiError> handleGameNotFound(
            GameNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RentalNotFoundException.class)
    ResponseEntity<ApiError> handleRentalNotFound(
            RentalNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RentalItemNotFoundException.class)
    ResponseEntity<ApiError> handleRentalItemNotFound(
            RentalItemNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(GameUnavailableException.class)
    ResponseEntity<ApiError> handleGameUnavailable(
            GameUnavailableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RentalItemAlreadyProcessedException.class)
    ResponseEntity<ApiError> handleRentalItemAlreadyProcessed(
            RentalItemAlreadyProcessedException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DuplicateReviewException.class)
    ResponseEntity<ApiError> handleDuplicateReview(
            DuplicateReviewException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ReviewEligibilityException.class)
    ResponseEntity<ApiError> handleReviewEligibility(
            ReviewEligibilityException exception,
            HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleInvalidParameter(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Invalid request parameter.", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                validationErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Request validation failed.", request, validationErrors);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
