package com.questhub.shared.presentation;

import com.questhub.shared.domain.BusinessException;
import com.questhub.shared.domain.DomainValidationException;
import com.questhub.shared.domain.ErrorCodes;
import com.questhub.shared.domain.FieldErrorItem;
import com.questhub.shared.domain.ResponseStatus;
import com.questhub.shared.presentation.dto.ApiError;
import com.questhub.shared.presentation.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(toHttpStatus(ex.getStatus()))
                .body(ApiResponse.error(ApiError.of(ex.getCode(), ex.getMessage(), ex.getDetails())));
    }

    private HttpStatus toHttpStatus(ResponseStatus status) {
        return switch (status) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainValidation(DomainValidationException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiError.of(ErrorCodes.VALIDATION_ERROR, ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return validationError(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBind(BindException ex) {
        return validationError(ex.getFieldErrors());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldErrorItem> details = ex.getConstraintViolations().stream()
                .map(violation -> new FieldErrorItem(
                        violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiResponse.error(
                ApiError.of(ErrorCodes.VALIDATION_ERROR, "Validation failed", details)));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ApiError.of(ErrorCodes.BAD_REQUEST, "Malformed request")));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ApiError.of(ErrorCodes.NOT_FOUND, "Resource not found")));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ApiError.of(ErrorCodes.CONFLICT, "Data conflict")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ApiError.of(ErrorCodes.FORBIDDEN, "Access denied")));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ApiError.of(ErrorCodes.UNAUTHORIZED, "Unauthenticated")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ApiError.of(ErrorCodes.INTERNAL_ERROR, "Internal server error")));
    }

    private ResponseEntity<ApiResponse<Void>> validationError(List<FieldError> fieldErrors) {
        List<FieldErrorItem> details = fieldErrors.stream()
                .map(fe -> new FieldErrorItem(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiResponse.error(
                ApiError.of(ErrorCodes.VALIDATION_ERROR, "Validation failed", details)));
    }
}